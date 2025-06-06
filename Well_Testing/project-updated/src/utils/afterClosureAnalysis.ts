import { TestData } from '../types/wellTest';
import { ClosurePoint } from './closureAnalysis';
import * as math from 'mathjs';

export type AfterClosureFlowRegime = 
  | 'impulse_linear'  // Impulse linear flow
  | 'impulse_radial'  // Impulse radial flow
  | 'unknown';        // Unknown flow regime

export interface AfterClosureAnalysisResult {
  flowRegime: AfterClosureFlowRegime;
  permeability: number;
  transmissibility: number;
  mobilityParameter: number;
  diffusivity: number;
  storageCoefficient: number;
  skinFactor: number;
  confidence: number; // 0-1 confidence score
}

/**
 * Performs after-closure analysis on DFIT data
 */
export function performAfterClosureAnalysis(
  testData: TestData,
  closurePoint: ClosurePoint,
  fluidViscosity: number = 1, // Default viscosity in cp
  totalCompressibility: number = 1e-5, // Default compressibility in 1/psi
  porosity: number = 0.1, // Default porosity (fraction)
  thickness: number = 100 // Default thickness in ft
): AfterClosureAnalysisResult {
  // Get data after closure
  const afterClosureData = {
    time: testData.time.slice(closurePoint.index),
    pressure: testData.pressure.slice(closurePoint.index)
  };
  
  // Identify flow regime
  const { flowRegime, confidence } = identifyAfterClosureFlowRegime(afterClosureData);
  
  // Calculate permeability based on flow regime
  let permeability = 0;
  let transmissibility = 0;
  let mobilityParameter = 0;
  let diffusivity = 0;
  let storageCoefficient = 0;
  let skinFactor = 0;
  
  if (flowRegime === 'impulse_linear') {
    const result = analyzeImpulseLinearFlow(
      afterClosureData,
      closurePoint.pressure,
      fluidViscosity,
      totalCompressibility,
      porosity,
      thickness
    );
    
    permeability = result.permeability;
    transmissibility = result.transmissibility;
    mobilityParameter = result.mobilityParameter;
    diffusivity = result.diffusivity;
    storageCoefficient = result.storageCoefficient;
    skinFactor = result.skinFactor;
  } else if (flowRegime === 'impulse_radial') {
    const result = analyzeImpulseRadialFlow(
      afterClosureData,
      closurePoint.pressure,
      fluidViscosity,
      totalCompressibility,
      porosity,
      thickness
    );
    
    permeability = result.permeability;
    transmissibility = result.transmissibility;
    mobilityParameter = result.mobilityParameter;
    diffusivity = result.diffusivity;
    storageCoefficient = result.storageCoefficient;
    skinFactor = result.skinFactor;
  }
  
  return {
    flowRegime,
    permeability,
    transmissibility,
    mobilityParameter,
    diffusivity,
    storageCoefficient,
    skinFactor,
    confidence
  };
}

/**
 * Identifies the after-closure flow regime
 */
function identifyAfterClosureFlowRegime(
  data: { time: number[]; pressure: number[] }
): { flowRegime: AfterClosureFlowRegime; confidence: number } {
  const { time, pressure } = data;
  
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
  
  // Calculate average slope
  const avgSlope = slopes.reduce((sum, slope) => sum + slope, 0) / slopes.length;
  
  // Calculate standard deviation of slopes
  const slopeVariance = slopes.reduce((sum, slope) => sum + Math.pow(slope - avgSlope, 2), 0) / slopes.length;
  const slopeStdDev = Math.sqrt(slopeVariance);
  
  // Determine flow regime based on slope
  let flowRegime: AfterClosureFlowRegime = 'unknown';
  let confidence = 0;
  
  // Impulse linear flow has a slope of -0.5
  const linearSlopeDiff = Math.abs(avgSlope + 0.5);
  
  // Impulse radial flow has a slope of -1.0
  const radialSlopeDiff = Math.abs(avgSlope + 1.0);
  
  if (linearSlopeDiff < radialSlopeDiff && linearSlopeDiff < 0.2) {
    flowRegime = 'impulse_linear';
    confidence = Math.max(0, 1 - linearSlopeDiff / 0.2);
  } else if (radialSlopeDiff < 0.2) {
    flowRegime = 'impulse_radial';
    confidence = Math.max(0, 1 - radialSlopeDiff / 0.2);
  }
  
  // Adjust confidence based on slope consistency
  const consistencyFactor = Math.max(0, 1 - slopeStdDev / Math.abs(avgSlope));
  confidence *= consistencyFactor;
  
  return { flowRegime, confidence };
}

/**
 * Analyzes impulse linear flow
 */
function analyzeImpulseLinearFlow(
  data: { time: number[]; pressure: number[] },
  closurePressure: number,
  fluidViscosity: number,
  totalCompressibility: number,
  porosity: number,
  thickness: number
): {
  permeability: number;
  transmissibility: number;
  mobilityParameter: number;
  diffusivity: number;
  storageCoefficient: number;
  skinFactor: number;
} {
  const { time, pressure } = data;
  
  // Calculate pressure difference from closure
  const deltaPressure = pressure.map(p => closurePressure - p);
  
  // Calculate square root of time
  const sqrtTime = time.map(t => Math.sqrt(t));
  
  // Perform linear regression on p vs sqrt(t)
  const regression = calculateLinearRegression(sqrtTime, deltaPressure);
  
  // Calculate permeability using impulse linear flow equation
  // k = (2.2 * μ) / (m * h * φ * ct)
  // where m is the slope of p vs sqrt(t)
  const slope = regression.slope;
  const permeability = (2.2 * fluidViscosity) / (Math.abs(slope) * thickness * porosity * totalCompressibility);
  
  // Calculate transmissibility (kh/μ)
  const transmissibility = permeability * thickness / fluidViscosity;
  
  // Calculate mobility parameter (k/μ)
  const mobilityParameter = permeability / fluidViscosity;
  
  // Calculate diffusivity (k / (φ * μ * ct))
  const diffusivity = permeability / (porosity * fluidViscosity * totalCompressibility);
  
  // Calculate storage coefficient (φ * ct * h)
  const storageCoefficient = porosity * totalCompressibility * thickness;
  
  // Estimate skin factor from intercept
  const intercept = regression.intercept;
  const skinFactor = calculateSkinFactor(intercept, permeability, porosity, totalCompressibility, fluidViscosity);
  
  return {
    permeability,
    transmissibility,
    mobilityParameter,
    diffusivity,
    storageCoefficient,
    skinFactor
  };
}

/**
 * Analyzes impulse radial flow
 */
function analyzeImpulseRadialFlow(
  data: { time: number[]; pressure: number[] },
  closurePressure: number,
  fluidViscosity: number,
  totalCompressibility: number,
  porosity: number,
  thickness: number
): {
  permeability: number;
  transmissibility: number;
  mobilityParameter: number;
  diffusivity: number;
  storageCoefficient: number;
  skinFactor: number;
} {
  const { time, pressure } = data;
  
  // Calculate pressure difference from closure
  const deltaPressure = pressure.map(p => closurePressure - p);
  
  // Calculate log of time
  const logTime = time.map(t => Math.log10(t));
  
  // Perform linear regression on p vs log(t)
  const regression = calculateLinearRegression(logTime, deltaPressure);
  
  // Calculate permeability using impulse radial flow equation
  // k = (162.6 * q * B * μ) / (m * h)
  // where m is the slope of p vs log(t)
  // For DFIT without rate, we use a simplified approach
  const slope = regression.slope;
  const permeability = (162.6 * fluidViscosity) / (Math.abs(slope) * thickness);
  
  // Calculate transmissibility (kh/μ)
  const transmissibility = permeability * thickness / fluidViscosity;
  
  // Calculate mobility parameter (k/μ)
  const mobilityParameter = permeability / fluidViscosity;
  
  // Calculate diffusivity (k / (φ * μ * ct))
  const diffusivity = permeability / (porosity * fluidViscosity * totalCompressibility);
  
  // Calculate storage coefficient (φ * ct * h)
  const storageCoefficient = porosity * totalCompressibility * thickness;
  
  // Estimate skin factor from intercept
  const intercept = regression.intercept;
  const skinFactor = calculateSkinFactor(intercept, permeability, porosity, totalCompressibility, fluidViscosity);
  
  return {
    permeability,
    transmissibility,
    mobilityParameter,
    diffusivity,
    storageCoefficient,
    skinFactor
  };
}

/**
 * Calculates skin factor from intercept
 */
function calculateSkinFactor(
  intercept: number,
  permeability: number,
  porosity: number,
  totalCompressibility: number,
  fluidViscosity: number
): number {
  // Simplified skin factor calculation
  // This is an approximation as actual skin calculation requires more parameters
  const diffusivity = permeability / (porosity * fluidViscosity * totalCompressibility);
  return Math.max(0, intercept * diffusivity / permeability - 0.5);
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

