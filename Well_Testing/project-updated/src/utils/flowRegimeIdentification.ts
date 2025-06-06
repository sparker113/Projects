import { TestData, FlowRegime } from '../types/wellTest';
import regression from 'regression';
import * as math from 'mathjs';
import { smoothData } from '../utils/dataPreprocessing';

/**
 * Identifies flow regime from pressure and derivative data
 */
export function identifyFlowRegime(testData: TestData): FlowRegime {
  console.log('Starting flow regime identification');
  
  const { time, pressure } = testData;
  
  // Calculate pressure derivative
  const derivative = calculatePressureDerivative(pressure, time);
  console.log('Initial derivative calculated:', derivative.length, 'points');
  
  // Apply advanced smoothing using Savitzky-Golay filter
  const smoothedDerivative = smoothDerivative(derivative);
  console.log('Smoothed derivative calculated:', smoothedDerivative.length, 'points');
  
  // Filter out invalid points and convert to log-log coordinates
  const logData = time.map((t, i) => {
    if (t <= 0 || smoothedDerivative[i] <= 0) {
      return null;
    }
    return [
      Math.log10(t),
      Math.log10(smoothedDerivative[i])
    ];
  }).filter((point): point is [number, number] => point !== null);
  
  console.log('Valid log-log points:', logData.length);
  
  // Check if we have enough valid points for regression
  if (logData.length < 2) {
    console.warn('Insufficient valid data points for flow regime identification');
    return 'unknown';
  }
  
  // Analyze derivative slope using regression
  const result = regression.linear(logData);
  const slope = result.equation[0];
  const r2 = result.r2;
  
  console.log('Regression results:', { slope, r2 });
  
  // Only identify flow regime if regression quality is acceptable
  if (r2 < 0.8) {
    console.warn(`Poor regression quality (R² = ${r2.toFixed(3)})`);
    return 'unknown';
  }
  
  // Identify flow regime based on derivative slope with tolerance
  const tolerance = 0.15;
  
  // Use more robust slope comparison with tolerance bands
  const slopeMatches = (target: number) => Math.abs(slope - target) < tolerance;
  
  if (slopeMatches(0)) {
    console.log('Identified radial flow (slope ≈ 0)');
    return 'radial_flow';
  }
  
  if (slopeMatches(0.5)) {
    console.log('Identified linear flow (slope ≈ 0.5)');
    return 'linear_flow';
  }
  
  if (slopeMatches(-0.5)) {
    console.log('Identified spherical flow (slope ≈ -0.5)');
    return 'spherical_flow';
  }
  
  if (slopeMatches(1)) {
    // Differentiate between wellbore storage and boundary dominated flow
    const medianTime = time[Math.floor(time.length / 2)];
    const isEarlyTime = medianTime < 1;
    
    console.log(`Identified unit slope regime at ${isEarlyTime ? 'early' : 'late'} time`);
    return isEarlyTime ? 'wellbore_storage' : 'boundary_dominated';
  }
  
  // Check for dual porosity behavior
  if (isDualPorositySignature(smoothedDerivative)) {
    console.log('Identified dual porosity signature');
    return 'dual_porosity';
  }
  
  console.log('No clear flow regime identified');
  return 'unknown';
}

/**
 * Calculates pressure derivative using Bourdet algorithm with improved handling
 */
export function calculatePressureDerivative(pressure: number[], time: number[]): number[] {
  console.log('Calculating pressure derivative');
  
  if (pressure.length !== time.length) {
    console.error('Pressure and time arrays must have same length');
    return new Array(pressure.length).fill(0);
  }
  
  const derivative: number[] = [];
  
  // Handle first point using forward difference
  const firstDt = time[1] - time[0];
  const firstDp = pressure[1] - pressure[0];
  derivative.push(firstDt > 0 ? (firstDp / firstDt) * time[0] : 0);
  
  // Calculate central points using Bourdet algorithm
  for (let i = 1; i < pressure.length - 1; i++) {
    const dt_prev = time[i] - time[i-1];
    const dt_next = time[i+1] - time[i];
    const dp_prev = pressure[i] - pressure[i-1];
    const dp_next = pressure[i+1] - pressure[i];
    
    if (dt_prev + dt_next > 0) {
      const derivative_point = 
        ((dp_next / dt_next) * dt_prev + (dp_prev / dt_prev) * dt_next) / 
        (dt_prev + dt_next);
      derivative.push(derivative_point * time[i]);
    } else {
      derivative.push(derivative[derivative.length - 1] || 0);
    }
  }
  
  // Handle last point using backward difference
  const lastDt = time[time.length - 1] - time[time.length - 2];
  const lastDp = pressure[pressure.length - 1] - pressure[pressure.length - 2];
  derivative.push(lastDt > 0 ? (lastDp / lastDt) * time[time.length - 1] : derivative[derivative.length - 1]);
  
  return derivative;
}

/**
 * Smooths pressure derivative using Savitzky-Golay filter
 */
export function smoothDerivative(derivative: number[], windowSize: number = 5): number[] {
  console.log('Smoothing derivative');
  
  // Ensure window size is odd
  windowSize = windowSize % 2 === 0 ? windowSize + 1 : windowSize;
  
  try {
    // Apply Savitzky-Golay smoothing
    const smoothed = smoothData(derivative, windowSize);
    
    // Remove negative values
    return smoothed.map(value => Math.max(0, value));
  } catch (error) {
    console.error('Error in derivative smoothing:', error);
    return derivative;
  }
}

/**
 * Detects dual porosity behavior
 */
function isDualPorositySignature(derivative: number[]): boolean {
  if (derivative.length < 6) return false;
  
  const n = derivative.length;
  const third = Math.floor(n / 3);
  
  // Calculate average values for each third
  const earlyAvg = math.mean(derivative.slice(0, third));
  const midAvg = math.mean(derivative.slice(third, 2 * third));
  const lateAvg = math.mean(derivative.slice(2 * third));
  
  // Check for characteristic "dip" in middle section
  const dipRatio = midAvg / ((earlyAvg + lateAvg) / 2);
  const slopeDifference = Math.abs((lateAvg - midAvg) - (midAvg - earlyAvg));
  
  console.log('Dual porosity check:', {
    earlyAvg,
    midAvg,
    lateAvg,
    dipRatio,
    slopeDifference
  });
  
  return dipRatio < 0.8 && slopeDifference < 0.2 * midAvg;
}