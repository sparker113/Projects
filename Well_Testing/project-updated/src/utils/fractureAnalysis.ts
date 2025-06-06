import { TestData, FluidProperties, WellProperties } from '../types/wellTest';
import * as math from 'mathjs';
import numeric from 'numeric';

interface FractureProperties {
  halfLength: number;
  conductivity: number;
  width: number;
  height: number;
  orientation: 'vertical' | 'horizontal';
  efficiency: number;
}

/**
 * Analyzes fracture properties from well test data
 */
export function analyzeFractureProperties(
  testData: TestData,
  fluidProps: FluidProperties,
  wellProps: WellProperties
): FractureProperties {
  const { pressure, time, flowRate } = testData;
  const { viscosity, formationVolumeFactor } = fluidProps;
  const { thickness } = wellProps;
  
  // Calculate slope from square root of time plot
  const sqrtTime = time.map(t => Math.sqrt(t));
  const slope = calculateLinearRegression(sqrtTime, pressure);
  
  // Calculate fracture half-length using slope analysis
  const halfLength = calculateFractureHalfLength(
    slope,
    flowRate[0],
    viscosity,
    formationVolumeFactor,
    thickness
  );
  
  // Calculate fracture conductivity using pressure derivative
  const conductivity = calculateFractureConductivity(
    pressure,
    time,
    flowRate,
    halfLength,
    viscosity,
    formationVolumeFactor
  );
  
  // Estimate fracture width using conductivity
  const width = estimateFractureWidth(conductivity, halfLength);
  
  // Estimate fracture height using pressure transient behavior
  const height = estimateFractureHeight(pressure, time, thickness);
  
  // Determine fracture orientation
  const orientation = determineOrientation(height, thickness);
  
  // Calculate fracture efficiency
  const efficiency = calculateFractureEfficiency(
    pressure,
    time,
    flowRate,
    halfLength,
    width,
    height
  );
  
  return {
    halfLength,
    conductivity,
    width,
    height,
    orientation,
    efficiency
  };
}

/**
 * Calculates fracture half-length
 */
function calculateFractureHalfLength(
  slope: number,
  flowRate: number,
  viscosity: number,
  formationVolumeFactor: number,
  thickness: number
): number {
  // Using linear flow equation for fracture half-length
  const constant = 4.064;
  const halfLength = Math.sqrt(
    (constant * flowRate * viscosity * formationVolumeFactor) /
    (thickness * slope)
  );
  
  return halfLength;
}

/**
 * Calculates fracture conductivity
 */
function calculateFractureConductivity(
  pressure: number[],
  time: number[],
  flowRate: number[],
  halfLength: number,
  viscosity: number,
  formationVolumeFactor: number
): number {
  // Using bilinear flow analysis for conductivity
  const bilinearSlope = calculateBilinearSlope(pressure, time);
  const conductivity = (
    Math.PI * flowRate[0] * viscosity * formationVolumeFactor *
    Math.sqrt(time[time.length - 1])
  ) / (4 * halfLength * bilinearSlope);
  
  return conductivity;
}

/**
 * Calculates bilinear flow slope
 */
function calculateBilinearSlope(pressure: number[], time: number[]): number {
  const fourthRootTime = time.map(t => Math.pow(t, 0.25));
  return calculateLinearRegression(fourthRootTime, pressure);
}

/**
 * Estimates fracture width using conductivity relationship
 */
function estimateFractureWidth(conductivity: number, halfLength: number): number {
  // Assuming a typical proppant permeability
  const proppantPermeability = 100000; // md
  return conductivity * halfLength / proppantPermeability;
}

/**
 * Estimates fracture height using pressure transient behavior
 */
function estimateFractureHeight(
  pressure: number[],
  time: number[],
  thickness: number
): number {
  // Using pressure derivative behavior to estimate height
  // This is a simplified approach and could be improved
  return thickness * 0.8; // Assuming fracture height is 80% of formation thickness
}

/**
 * Determines fracture orientation based on height and thickness
 */
function determineOrientation(height: number, thickness: number): 'vertical' | 'horizontal' {
  return height > thickness * 0.5 ? 'vertical' : 'horizontal';
}

/**
 * Calculates fracture efficiency
 */
function calculateFractureEfficiency(
  pressure: number[],
  time: number[],
  flowRate: number[],
  halfLength: number,
  width: number,
  height: number
): number {
  // Calculate created fracture volume
  const fractureVolume = 2 * halfLength * width * height;
  
  // Calculate total injected volume
  const totalInjectedVolume = flowRate.reduce((sum, rate, i) => {
    const dt = i < time.length - 1 ? time[i + 1] - time[i] : 0;
    return sum + rate * dt;
  }, 0);
  
  return Math.min(fractureVolume / totalInjectedVolume, 1);
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