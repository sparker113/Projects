import { TestData } from '../types/wellTest';
import { calculatePressureDerivative } from './flowRegimeIdentification';
import * as math from 'mathjs';

interface DFITResults {
  isip: number;
  closurePressure: number;
  minStress: number;
  leakOffCoefficient: number;
  permeability: number;
  fracturePressure: number;
  processZone: number;
  gFunction: { time: number; pressure: number; derivative: number }[];
}

/**
 * Analyzes DFIT data to calculate key parameters
 */
export function analyzeDFIT(testData: TestData): DFITResults {
  const { pressure, time } = testData;
  
  // Calculate ISIP (Instantaneous Shut-In Pressure)
  const isip = calculateISIP(pressure, time);
  
  // Calculate G-function and its derivative
  const gFunction = calculateGFunction(pressure, time);
  
  // Detect closure pressure from G-function analysis
  const closurePressure = detectClosurePressure(gFunction);
  
  // Calculate minimum stress
  const minStress = calculateMinimumStress(closurePressure);
  
  // Calculate leak-off coefficient
  const leakOffCoefficient = calculateLeakOffCoefficient(gFunction, closurePressure);
  
  // Calculate permeability from after-closure analysis
  const permeability = calculatePermeabilityFromAfterClosure(pressure, time, closurePressure);
  
  // Calculate fracture pressure
  const fracturePressure = calculateFracturePressure(pressure, time);
  
  // Calculate process zone stress
  const processZone = calculateProcessZoneStress(isip, closurePressure);
  
  return {
    isip,
    closurePressure,
    minStress,
    leakOffCoefficient,
    permeability,
    fracturePressure,
    processZone,
    gFunction
  };
}

/**
 * Calculates ISIP from pressure decline data
 */
function calculateISIP(pressure: number[], time: number[]): number {
  // Find maximum pressure and corresponding time
  const maxPressureIndex = pressure.indexOf(Math.max(...pressure));
  
  // Calculate pressure derivative
  const derivative = calculatePressureDerivative(pressure, time);
  
  // Find inflection point after maximum pressure
  let isipIndex = maxPressureIndex;
  for (let i = maxPressureIndex + 1; i < derivative.length - 1; i++) {
    if (Math.abs(derivative[i]) < Math.abs(derivative[i - 1]) * 0.1) {
      isipIndex = i;
      break;
    }
  }
  
  return pressure[isipIndex];
}

/**
 * Calculates G-function and its derivative
 */
function calculateGFunction(pressure: number[], time: number[]): {
  time: number;
  pressure: number;
  derivative: number;
}[] {
  const g0 = 0.469; // G-function constant
  const gFunction = [];
  
  // Calculate superposition time function
  for (let i = 0; i < time.length; i++) {
    const deltaT = time[i] - time[0];
    const g = g0 * (4 * deltaT / time[0] * (1 + deltaT / time[0]));
    gFunction.push({
      time: g,
      pressure: pressure[i],
      derivative: 0 // Will be calculated below
    });
  }
  
  // Calculate G-function derivative
  for (let i = 1; i < gFunction.length - 1; i++) {
    const dP = gFunction[i + 1].pressure - gFunction[i - 1].pressure;
    const dG = gFunction[i + 1].time - gFunction[i - 1].time;
    gFunction[i].derivative = dP / dG;
  }
  
  // Handle endpoints
  gFunction[0].derivative = gFunction[1].derivative;
  gFunction[gFunction.length - 1].derivative = gFunction[gFunction.length - 2].derivative;
  
  return gFunction;
}

/**
 * Detects closure pressure from G-function analysis
 */
function detectClosurePressure(gFunction: {
  time: number;
  pressure: number;
  derivative: number;
}[]): number {
  // Find departure from linear trend in G-function derivative
  let closureIndex = 0;
  let maxDeviation = 0;
  
  for (let i = 1; i < gFunction.length - 1; i++) {
    const expected = gFunction[i - 1].derivative;
    const actual = gFunction[i].derivative;
    const deviation = Math.abs(actual - expected);
    
    if (deviation > maxDeviation) {
      maxDeviation = deviation;
      closureIndex = i;
    }
  }
  
  return gFunction[closureIndex].pressure;
}

/**
 * Calculates minimum stress from closure pressure
 */
function calculateMinimumStress(closurePressure: number): number {
  // Minimum stress is approximately equal to closure pressure
  // Adjust for hydrostatic column if needed
  return closurePressure;
}

/**
 * Calculates leak-off coefficient from G-function analysis
 */
function calculateLeakOffCoefficient(
  gFunction: { time: number; pressure: number; derivative: number }[],
  closurePressure: number
): number {
  // Find slope before closure
  const closureIndex = gFunction.findIndex(g => g.pressure <= closurePressure);
  const beforeClosure = gFunction.slice(0, closureIndex);
  
  // Calculate slope using linear regression
  const x = beforeClosure.map(g => g.time);
  const y = beforeClosure.map(g => g.pressure);
  const slope = calculateLinearRegression(x, y);
  
  // Convert slope to leak-off coefficient
  return Math.abs(slope) * 0.0374; // Conversion factor to standard units
}

/**
 * Calculates permeability from after-closure analysis
 */
function calculatePermeabilityFromAfterClosure(
  pressure: number[],
  time: number[],
  closurePressure: number
): number {
  // Find after-closure data
  const closureIndex = pressure.findIndex(p => p <= closurePressure);
  const afterClosure = {
    pressure: pressure.slice(closureIndex),
    time: time.slice(closureIndex)
  };
  
  // Calculate slope using square root of time plot
  const sqrtTime = afterClosure.time.map(t => Math.sqrt(t));
  const slope = calculateLinearRegression(sqrtTime, afterClosure.pressure);
  
  // Convert slope to permeability using after-closure analysis equation
  return Math.abs(slope) * 0.0374; // Conversion factor to mD
}

/**
 * Calculates fracture pressure
 */
function calculateFracturePressure(pressure: number[], time: number[]): number {
  return Math.max(...pressure);
}

/**
 * Calculates process zone stress
 */
function calculateProcessZoneStress(isip: number, closurePressure: number): number {
  return isip - closurePressure;
}

/**
 * Calculates linear regression slope
 */
function calculateLinearRegression(x: number[], y: number[]): number {
  const n = x.length;
  const sumX = x.reduce((a, b) => a + b, 0);
  const sumY = y.reduce((a, b) => a + b, 0);
  const sumXY = x.reduce((sum, xi, i) => sum + xi * y[i], 0);
  const sumXX = x.reduce((sum, xi) => sum + xi * xi, 0);
  
  return (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
}