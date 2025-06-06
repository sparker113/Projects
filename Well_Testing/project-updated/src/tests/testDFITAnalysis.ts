import { TestData } from '../types/wellTest';
import { detectClosure, performClosureAnalysis } from '../utils/closureAnalysis';
import { performAfterClosureAnalysis } from '../utils/afterClosureAnalysis';
import { calculateTimeFunction } from '../utils/timeFunctions';

/**
 * Test script to verify the functionality of the updated DFIT analysis
 */

// Sample DFIT data without flow rate
const sampleDFITData: TestData = {
  time: [
    0, 0.1, 0.2, 0.5, 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000
  ],
  pressure: [
    5000, 4950, 4900, 4850, 4800, 4750, 4700, 4650, 4600, 4550, 4500, 4450, 4400, 4350, 4300, 4250
  ],
  pressureUnit: 'psi',
  timeUnit: 'min',
  flowRateUnit: 'bbl/d',
  // Note: No flow rate data for DFIT
};

// Test time functions
console.log('Testing time functions...');
const timeFunctions = [
  'elapsed_time',
  'log_time',
  'sqrt_time',
  'squared_time',
  'reciprocal_time',
  'superposition_time',
  'horner_time',
  'agarwal_time'
] as const;

for (const timeFunction of timeFunctions) {
  try {
    const result = calculateTimeFunction(sampleDFITData, timeFunction);
    console.log(`${timeFunction}: ${result.slice(0, 5).join(', ')}...`);
  } catch (error) {
    console.error(`Error calculating ${timeFunction}:`, error);
  }
}

// Test closure analysis
console.log('\nTesting closure analysis...');
const closureMethods = [
  'g-function',
  'square-root-time',
  'log-log',
  'derivative',
  'tangent'
] as const;

for (const method of closureMethods) {
  try {
    const result = detectClosure(sampleDFITData, method);
    console.log(`${method} closure detection: time=${result.time}, pressure=${result.pressure}, confidence=${result.confidence}`);
  } catch (error) {
    console.error(`Error detecting closure using ${method}:`, error);
  }
}

// Test comprehensive closure analysis
console.log('\nTesting comprehensive closure analysis...');
try {
  const result = performClosureAnalysis(sampleDFITData);
  console.log('Closure analysis result:', {
    closurePoint: {
      time: result.closurePoint.time,
      pressure: result.closurePoint.pressure,
      method: result.closurePoint.method,
      confidence: result.closurePoint.confidence
    },
    isip: result.isip,
    fracturePressure: result.fracturePressure,
    minStress: result.minStress,
    processZoneStress: result.processZoneStress,
    leakOffCoefficient: result.leakOffCoefficient
  });
} catch (error) {
  console.error('Error performing comprehensive closure analysis:', error);
}

// Test after-closure analysis
console.log('\nTesting after-closure analysis...');
try {
  // First get closure point
  const closurePoint = detectClosure(sampleDFITData, 'g-function');
  
  // Then perform after-closure analysis
  const result = performAfterClosureAnalysis(
    sampleDFITData,
    closurePoint,
    1, // viscosity
    1e-5, // compressibility
    0.1, // porosity
    100 // thickness
  );
  
  console.log('After-closure analysis result:', {
    flowRegime: result.flowRegime,
    permeability: result.permeability,
    transmissibility: result.transmissibility,
    mobilityParameter: result.mobilityParameter,
    diffusivity: result.diffusivity,
    storageCoefficient: result.storageCoefficient,
    skinFactor: result.skinFactor,
    confidence: result.confidence
  });
} catch (error) {
  console.error('Error performing after-closure analysis:', error);
}

console.log('\nAll tests completed.');

