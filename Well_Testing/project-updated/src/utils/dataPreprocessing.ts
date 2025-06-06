import { TestData } from '../types/wellTest';
import { convertArrayToFieldUnits } from './unitConversion';
import * as math from 'mathjs';

/**
 * Validates and preprocesses test data
 */
export function preprocessTestData(data: TestData): {
  isValid: boolean;
  errors: string[];
  processedData?: TestData;
} {
  const errors: string[] = [];
  
  // Check array lengths match
  if (data.pressure.length !== data.time.length || data.pressure.length !== data.flowRate.length) {
    errors.push('Pressure, time, and flow rate arrays must have the same length');
    return { isValid: false, errors };
  }
  
  // Check for minimum data points
  if (data.pressure.length < 10) {
    errors.push('At least 10 data points are required for reliable analysis');
    return { isValid: false, errors };
  }
  
  // Check time values are monotonically increasing
  for (let i = 1; i < data.time.length; i++) {
    if (data.time[i] <= data.time[i-1]) {
      errors.push('Time values must be monotonically increasing');
      return { isValid: false, errors };
    }
  }
  
  // Check for physically reasonable values
  if (data.pressure.some(p => p <= 0)) {
    errors.push('Pressure values must be positive');
    return { isValid: false, errors };
  }
  
  if (data.time.some(t => t < 0)) {
    errors.push('Time values cannot be negative');
    return { isValid: false, errors };
  }
  
  // Convert all values to field units
  const convertedPressure = convertArrayToFieldUnits(data.pressure, data.pressureUnit, 'pressure');
  const convertedTime = convertArrayToFieldUnits(data.time, data.timeUnit, 'time');
  const convertedFlowRate = convertArrayToFieldUnits(data.flowRate, data.flowRateUnit, 'flowRate');
  
  // Apply Savitzky-Golay smoothing to pressure data
  let windowSize = Math.min(21, Math.floor(data.pressure.length / 3));
  if (windowSize % 2 === 0) windowSize--; // Ensure odd window size
  
  const smoothedPressure = smoothData(convertedPressure, windowSize);
  
  // Remove outliers using modified z-score method
  const { cleanedData, outlierIndices } = removeOutliers(smoothedPressure);
  
  if (outlierIndices.length > 0) {
    console.warn(`Removed ${outlierIndices.length} outliers from pressure data`);
  }
  
  // Create processed data object
  const processedData: TestData = {
    pressure: cleanedData,
    time: convertedTime.filter((_, i) => !outlierIndices.includes(i)),
    flowRate: convertedFlowRate.filter((_, i) => !outlierIndices.includes(i)),
    pressureUnit: 'psi',
    timeUnit: 'hours',
    flowRateUnit: 'STB/day'
  };
  
  return {
    isValid: true,
    errors,
    processedData
  };
}

/**
 * Implements polynomial regression using matrix operations
 */
function polynomialRegression(x: number[], y: number[], order: number): number[] {
  // Create Vandermonde matrix
  const X = math.matrix(x.map(xi => {
    const row = [];
    for (let p = 0; p <= order; p++) {
      row.push(Math.pow(xi, p));
    }
    return row;
  }));
  
  // Convert y to matrix
  const Y = math.matrix(y.map(yi => [yi]));
  
  // Calculate coefficients using normal equation: coeffs = (X'X)^(-1)X'y
  const Xt = math.transpose(X);
  const XtX = math.multiply(Xt, X);
  const XtX_inv = math.inv(XtX);
  const Xty = math.multiply(Xt, Y);
  const coeffs = math.multiply(XtX_inv, Xty);
  
  // Convert matrix to array and reverse to get coefficients in descending order
  return math.flatten(coeffs).toArray().reverse();
}

/**
 * Applies Savitzky-Golay smoothing to data
 */
export function smoothData(data: number[], windowSize: number, polynomialOrder: number = 3): number[] {
  const halfWindow = Math.floor(windowSize / 2);
  const smoothed = [...data];
  
  for (let i = halfWindow; i < data.length - halfWindow; i++) {
    const window = data.slice(i - halfWindow, i + halfWindow + 1);
    const x = Array.from({ length: windowSize }, (_, j) => j - halfWindow);
    
    // Fit polynomial to window
    const coeffs = polynomialRegression(x, window, polynomialOrder);
    
    // Evaluate polynomial at x = 0 (center point)
    smoothed[i] = coeffs[polynomialOrder]; // Constant term is the last coefficient
  }
  
  // Handle edges by reflecting data
  for (let i = 0; i < halfWindow; i++) {
    smoothed[i] = smoothed[halfWindow];
    smoothed[data.length - 1 - i] = smoothed[data.length - 1 - halfWindow];
  }
  
  return smoothed;
}

/**
 * Removes outliers using modified z-score method
 */
function removeOutliers(
  data: number[],
  threshold: number = 3.5
): { cleanedData: number[]; outlierIndices: number[] } {
  const median = math.median(data);
  const mad = math.median(data.map(x => Math.abs(x - median)));
  const modifiedZScores = data.map(x => 0.6745 * (x - median) / mad);
  
  const outlierIndices: number[] = [];
  const cleanedData = data.filter((_, i) => {
    const isOutlier = Math.abs(modifiedZScores[i]) > threshold;
    if (isOutlier) outlierIndices.push(i);
    return !isOutlier;
  });
  
  return { cleanedData, outlierIndices };
}

/**
 * Calculates basic statistics for data quality assessment
 */
export function calculateDataQuality(data: number[]): {
  mean: number;
  standardDeviation: number;
  coefficientOfVariation: number;
  signalToNoiseRatio: number;
} {
  const mean = math.mean(data);
  const standardDeviation = math.std(data);
  const coefficientOfVariation = standardDeviation / mean;
  const signalToNoiseRatio = mean / standardDeviation;
  
  return {
    mean,
    standardDeviation,
    coefficientOfVariation,
    signalToNoiseRatio
  };
}