import { TestData } from '../types/wellTest';
import { calculateTimeFunction } from './timeFunctions';
import * as math from 'mathjs';

export type ClosureAnalysisMethod = 
  | 'g-function'        // G-function analysis
  | 'square-root-time'  // Square root of time analysis
  | 'log-log'           // Log-log analysis
  | 'derivative'        // Derivative analysis
  | 'tangent';          // Tangent method

export interface ClosurePoint {
  index: number;
  time: number;
  pressure: number;
  method: ClosureAnalysisMethod;
  confidence: number; // 0-1 confidence score
}

export interface ClosureAnalysisResult {
  closurePoint: ClosurePoint;
  isip: number;
  fracturePressure: number;
  minStress: number;
  processZoneStress: number;
  leakOffCoefficient: number;
}

/**
 * Analyzes DFIT data to detect closure pressure using various methods
 */
export function detectClosure(
  testData: TestData,
  method: ClosureAnalysisMethod
): ClosurePoint {
  switch (method) {
    case 'g-function':
      return detectClosureUsingGFunction(testData);
    case 'square-root-time':
      return detectClosureUsingSquareRootTime(testData);
    case 'log-log':
      return detectClosureUsingLogLog(testData);
    case 'derivative':
      return detectClosureUsingDerivative(testData);
    case 'tangent':
      return detectClosureUsingTangentMethod(testData);
    default:
      return detectClosureUsingGFunction(testData);
  }
}

/**
 * Performs comprehensive closure analysis using all methods
 */
export function performClosureAnalysis(testData: TestData): ClosureAnalysisResult {
  // Detect closure using all methods
  const gFunctionClosure = detectClosureUsingGFunction(testData);
  const sqrtTimeClosure = detectClosureUsingSquareRootTime(testData);
  const logLogClosure = detectClosureUsingLogLog(testData);
  const derivativeClosure = detectClosureUsingDerivative(testData);
  const tangentClosure = detectClosureUsingTangentMethod(testData);
  
  // Select the closure point with highest confidence
  const closurePoints = [
    gFunctionClosure,
    sqrtTimeClosure,
    logLogClosure,
    derivativeClosure,
    tangentClosure
  ];
  
  const bestClosurePoint = closurePoints.reduce((best, current) => 
    current.confidence > best.confidence ? current : best, 
    closurePoints[0]
  );
  
  // Calculate ISIP (Instantaneous Shut-In Pressure)
  const isip = calculateISIP(testData);
  
  // Calculate fracture pressure (maximum pressure)
  const fracturePressure = Math.max(...testData.pressure);
  
  // Calculate minimum stress (approximately equal to closure pressure)
  const minStress = bestClosurePoint.pressure;
  
  // Calculate process zone stress
  const processZoneStress = isip - minStress;
  
  // Calculate leak-off coefficient
  const leakOffCoefficient = calculateLeakOffCoefficient(testData, bestClosurePoint);
  
  return {
    closurePoint: bestClosurePoint,
    isip,
    fracturePressure,
    minStress,
    processZoneStress,
    leakOffCoefficient
  };
}

/**
 * Detects closure using G-function analysis
 */
function detectClosureUsingGFunction(testData: TestData): ClosurePoint {
  const { time, pressure } = testData;
  
  // Calculate G-function values
  const gFunction = calculateGFunction(testData);
  
  // Calculate G-function derivative
  const derivatives: number[] = [];
  for (let i = 1; i < gFunction.length - 1; i++) {
    const dP = gFunction[i + 1].pressure - gFunction[i - 1].pressure;
    const dG = gFunction[i + 1].g - gFunction[i - 1].g;
    derivatives.push(dP / dG);
  }
  
  // Add endpoints
  derivatives.unshift(derivatives[0] || 0);
  derivatives.push(derivatives[derivatives.length - 1] || 0);
  
  // Find departure from linear trend in G-function derivative
  let closureIndex = 0;
  let maxDeviation = 0;
  let confidence = 0;
  
  for (let i = 1; i < derivatives.length - 1; i++) {
    const expected = derivatives[i - 1];
    const actual = derivatives[i];
    const deviation = Math.abs(actual - expected);
    
    if (deviation > maxDeviation) {
      maxDeviation = deviation;
      closureIndex = i;
      confidence = Math.min(deviation / expected, 1);
    }
  }
  
  return {
    index: closureIndex,
    time: time[closureIndex],
    pressure: pressure[closureIndex],
    method: 'g-function',
    confidence
  };
}

/**
 * Detects closure using square root of time analysis
 */
function detectClosureUsingSquareRootTime(testData: TestData): ClosurePoint {
  const { time, pressure } = testData;
  
  // Calculate square root of time
  const sqrtTime = calculateTimeFunction(testData, 'sqrt_time');
  
  // Calculate pressure derivative with respect to sqrt(time)
  const derivatives: number[] = [];
  for (let i = 1; i < time.length - 1; i++) {
    const dP = pressure[i + 1] - pressure[i - 1];
    const dSqrtT = sqrtTime[i + 1] - sqrtTime[i - 1];
    derivatives.push(dP / dSqrtT);
  }
  
  // Add endpoints
  derivatives.unshift(derivatives[0] || 0);
  derivatives.push(derivatives[derivatives.length - 1] || 0);
  
  // Find inflection point in derivative
  let closureIndex = 0;
  let maxCurvature = 0;
  let confidence = 0;
  
  for (let i = 1; i < derivatives.length - 1; i++) {
    const curvature = Math.abs(derivatives[i + 1] - 2 * derivatives[i] + derivatives[i - 1]);
    
    if (curvature > maxCurvature) {
      maxCurvature = curvature;
      closureIndex = i;
      confidence = Math.min(curvature / derivatives[i], 1);
    }
  }
  
  return {
    index: closureIndex,
    time: time[closureIndex],
    pressure: pressure[closureIndex],
    method: 'square-root-time',
    confidence
  };
}

/**
 * Detects closure using log-log analysis
 */
function detectClosureUsingLogLog(testData: TestData): ClosurePoint {
  const { time, pressure } = testData;
  
  // Calculate log-log values
  const logTime = time.map(t => Math.log10(Math.max(t, 1e-10)));
  const logPressure = pressure.map(p => Math.log10(Math.max(p, 1e-10)));
  
  // Calculate slopes between consecutive points
  const slopes: number[] = [];
  for (let i = 1; i < logTime.length; i++) {
    const dLogP = logPressure[i] - logPressure[i - 1];
    const dLogT = logTime[i] - logTime[i - 1];
    slopes.push(dLogP / dLogT);
  }
  
  // Add endpoint
  slopes.push(slopes[slopes.length - 1] || 0);
  
  // Find transition from 1/2 slope to 1 slope
  let closureIndex = 0;
  let maxSlopeChange = 0;
  let confidence = 0;
  
  for (let i = 1; i < slopes.length - 1; i++) {
    const slopeChange = Math.abs(slopes[i] - slopes[i - 1]);
    const targetSlopeChange = Math.abs(slopes[i] - 0.5);
    
    if (slopeChange > maxSlopeChange && targetSlopeChange < 0.3) {
      maxSlopeChange = slopeChange;
      closureIndex = i;
      confidence = Math.min(slopeChange, 1);
    }
  }
  
  return {
    index: closureIndex,
    time: time[closureIndex],
    pressure: pressure[closureIndex],
    method: 'log-log',
    confidence
  };
}

/**
 * Detects closure using pressure derivative analysis
 */
function detectClosureUsingDerivative(testData: TestData): ClosurePoint {
  const { time, pressure } = testData;
  
  // Calculate pressure derivative
  const derivatives: number[] = [];
  for (let i = 1; i < time.length - 1; i++) {
    const dP = pressure[i + 1] - pressure[i - 1];
    const dT = time[i + 1] - time[i - 1];
    derivatives.push(dP / dT);
  }
  
  // Add endpoints
  derivatives.unshift(derivatives[0] || 0);
  derivatives.push(derivatives[derivatives.length - 1] || 0);
  
  // Find minimum in derivative
  let closureIndex = 0;
  let minDerivative = Infinity;
  let confidence = 0;
  
  for (let i = 1; i < derivatives.length - 1; i++) {
    if (derivatives[i] < minDerivative) {
      minDerivative = derivatives[i];
      closureIndex = i;
      confidence = Math.min(1, Math.abs((derivatives[i - 1] - derivatives[i]) / derivatives[i - 1]));
    }
  }
  
  return {
    index: closureIndex,
    time: time[closureIndex],
    pressure: pressure[closureIndex],
    method: 'derivative',
    confidence
  };
}

/**
 * Detects closure using tangent method
 */
function detectClosureUsingTangentMethod(testData: TestData): ClosurePoint {
  const { time, pressure } = testData;
  
  // Find maximum pressure
  const maxPressureIndex = pressure.indexOf(Math.max(...pressure));
  
  // Calculate slopes for early and late time regions
  const earlyTimeSlope = calculateLinearRegression(
    time.slice(0, Math.floor(time.length / 3)),
    pressure.slice(0, Math.floor(pressure.length / 3))
  );
  
  const lateTimeSlope = calculateLinearRegression(
    time.slice(Math.floor(2 * time.length / 3)),
    pressure.slice(Math.floor(2 * pressure.length / 3))
  );
  
  // Find intersection of the two tangent lines
  let closureIndex = maxPressureIndex;
  let minDistance = Infinity;
  let confidence = 0;
  
  for (let i = maxPressureIndex; i < time.length; i++) {
    const earlyLineValue = earlyTimeSlope.slope * time[i] + earlyTimeSlope.intercept;
    const lateLineValue = lateTimeSlope.slope * time[i] + lateTimeSlope.intercept;
    const distance = Math.abs(earlyLineValue - lateLineValue);
    
    if (distance < minDistance) {
      minDistance = distance;
      closureIndex = i;
      confidence = Math.min(1, 1 - distance / pressure[i]);
    }
  }
  
  return {
    index: closureIndex,
    time: time[closureIndex],
    pressure: pressure[closureIndex],
    method: 'tangent',
    confidence
  };
}

/**
 * Calculates G-function values
 */
function calculateGFunction(testData: TestData): { time: number; g: number; pressure: number }[] {
  const { time, pressure } = testData;
  const g0 = 0.469; // G-function constant
  
  // Assume the first point is the shut-in time for DFIT
  // If the first time is 0, use the second time point as shut-in time
  const shutInIndex = time[0] === 0 ? 1 : 0;
  const shutInTime = time[shutInIndex];
  const shutInPressure = pressure[shutInIndex];
  
  return time.map((t, i) => {
    const deltaT = t - shutInTime;
    if (deltaT <= 0) {
      return { time: t, g: 0, pressure: pressure[i] };
    }
    
    // Calculate g-function using standard formula
    const g = g0 * (4 * deltaT / shutInTime * (1 + deltaT / shutInTime));
    
    return {
      time: t,
      g,
      pressure: pressure[i]
    };
  });
}

/**
 * Calculates ISIP from pressure decline data
 */
function calculateISIP(testData: TestData): number {
  const { pressure } = testData;
  
  // For DFIT, ISIP is approximately the maximum pressure
  return Math.max(...pressure);
}

/**
 * Calculates leak-off coefficient from closure point
 */
function calculateLeakOffCoefficient(testData: TestData, closurePoint: ClosurePoint): number {
  const { time, pressure } = testData;
  
  // Get data before closure
  const beforeClosure = {
    time: time.slice(0, closurePoint.index),
    pressure: pressure.slice(0, closurePoint.index)
  };
  
  // Calculate square root of time
  const sqrtTime = beforeClosure.time.map(t => Math.sqrt(t));
  
  // Calculate slope using linear regression
  const regression = calculateLinearRegression(sqrtTime, beforeClosure.pressure);
  
  // Convert slope to leak-off coefficient
  return Math.abs(regression.slope) * 0.0374; // Conversion factor to standard units
}

/**
 * Calculates linear regression
 */
function calculateLinearRegression(x: number[], y: number[]): { slope: number; intercept: number; r2: number } {
  const n = x.length;
  
  if (n < 2) {
    return { slope: 0, intercept: 0, r2: 0 };
  }
  
  const sumX = x.reduce((a, b) => a + b, 0);
  const sumY = y.reduce((a, b) => a + b, 0);
  const sumXY = x.reduce((sum, xi, i) => sum + xi * y[i], 0);
  const sumXX = x.reduce((sum, xi) => sum + xi * xi, 0);
  const sumYY = y.reduce((sum, yi) => sum + yi * yi, 0);
  
  const slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
  const intercept = (sumY - slope * sumX) / n;
  
  // Calculate R-squared
  const yMean = sumY / n;
  const totalVariation = y.reduce((sum, yi) => sum + Math.pow(yi - yMean, 2), 0);
  const explainedVariation = y.reduce((sum, yi, i) => {
    const predicted = slope * x[i] + intercept;
    return sum + Math.pow(predicted - yMean, 2);
  }, 0);
  
  const r2 = explainedVariation / totalVariation;
  
  return { slope, intercept, r2 };
}

