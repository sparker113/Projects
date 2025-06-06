// Test script to verify the functionality of the updated DFIT analysis

// Sample DFIT data without flow rate
const sampleDFITData = {
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

// Manual test of the time functions
console.log('Testing time functions manually...');
const sqrtTime = sampleDFITData.time.map(t => Math.sqrt(t));
console.log('sqrt_time:', sqrtTime.slice(0, 5));

const logTime = sampleDFITData.time.map(t => Math.log10(Math.max(t, 1e-10)));
console.log('log_time:', logTime.slice(0, 5));

// Manual test of closure detection
console.log('\nTesting closure detection manually...');
// G-function calculation
const g0 = 0.469; // G-function constant
const shutInTime = sampleDFITData.time[0];
const shutInPressure = sampleDFITData.pressure[0];

const gFunction = sampleDFITData.time.map((t, i) => {
  const deltaT = t - shutInTime;
  if (deltaT <= 0) return { time: t, g: 0, pressure: sampleDFITData.pressure[i] };
  
  // Calculate g-function using standard formula
  const g = g0 * (4 * deltaT / shutInTime * (1 + deltaT / shutInTime));
  
  return {
    time: t,
    g,
    pressure: sampleDFITData.pressure[i]
  };
});

console.log('G-function values:', gFunction.slice(0, 5));

// Calculate G-function derivative
const derivatives = [];
for (let i = 1; i < gFunction.length - 1; i++) {
  const dP = gFunction[i + 1].pressure - gFunction[i - 1].pressure;
  const dG = gFunction[i + 1].g - gFunction[i - 1].g;
  derivatives.push(dP / dG);
}

// Add endpoints
derivatives.unshift(derivatives[0] || 0);
derivatives.push(derivatives[derivatives.length - 1] || 0);

console.log('G-function derivatives:', derivatives.slice(0, 5));

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

console.log('Detected closure point:', {
  index: closureIndex,
  time: sampleDFITData.time[closureIndex],
  pressure: sampleDFITData.pressure[closureIndex],
  confidence
});

// Manual test of after-closure analysis
console.log('\nTesting after-closure analysis manually...');
// Get data after closure
const afterClosureTime = sampleDFITData.time.slice(closureIndex);
const afterClosurePressure = sampleDFITData.pressure.slice(closureIndex);

// Calculate pressure difference from closure
const closurePressure = sampleDFITData.pressure[closureIndex];
const deltaPressure = afterClosurePressure.map(p => closurePressure - p);

// Calculate square root of time for linear flow
const afterClosureSqrtTime = afterClosureTime.map(t => Math.sqrt(t));

console.log('After-closure data:', {
  time: afterClosureTime.slice(0, 5),
  pressure: afterClosurePressure.slice(0, 5),
  deltaPressure: deltaPressure.slice(0, 5),
  sqrtTime: afterClosureSqrtTime.slice(0, 5)
});

// Calculate log-log values for flow regime identification
const logAfterClosureTime = afterClosureTime.map(t => Math.log10(Math.max(t, 1e-10)));
const logDeltaPressure = deltaPressure.map(p => Math.log10(Math.max(Math.abs(p), 1e-10)));

// Calculate slopes between consecutive points
const slopes = [];
for (let i = 1; i < logAfterClosureTime.length; i++) {
  const dLogP = logDeltaPressure[i] - logDeltaPressure[i - 1];
  const dLogT = logAfterClosureTime[i] - logAfterClosureTime[i - 1];
  slopes.push(dLogP / dLogT);
}

// Calculate average slope
const avgSlope = slopes.reduce((sum, slope) => sum + slope, 0) / slopes.length;

console.log('Log-log slope analysis:', {
  slopes: slopes.slice(0, 5),
  avgSlope
});

// Determine flow regime based on slope
let flowRegime = 'unknown';
let flowConfidence = 0;

// Impulse linear flow has a slope of -0.5
const linearSlopeDiff = Math.abs(avgSlope + 0.5);

// Impulse radial flow has a slope of -1.0
const radialSlopeDiff = Math.abs(avgSlope + 1.0);

if (linearSlopeDiff < radialSlopeDiff && linearSlopeDiff < 0.2) {
  flowRegime = 'impulse_linear';
  flowConfidence = Math.max(0, 1 - linearSlopeDiff / 0.2);
} else if (radialSlopeDiff < 0.2) {
  flowRegime = 'impulse_radial';
  flowConfidence = Math.max(0, 1 - radialSlopeDiff / 0.2);
}

console.log('Detected flow regime:', {
  flowRegime,
  confidence: flowConfidence
});

// Calculate permeability for impulse linear flow
const fluidViscosity = 1; // cp
const totalCompressibility = 1e-5; // 1/psi
const porosity = 0.1; // fraction
const thickness = 100; // ft

// Perform linear regression on p vs sqrt(t)
const n = afterClosureSqrtTime.length;
const sumX = afterClosureSqrtTime.reduce((a, b) => a + b, 0);
const sumY = deltaPressure.reduce((a, b) => a + b, 0);
const sumXY = afterClosureSqrtTime.reduce((sum, xi, i) => sum + xi * deltaPressure[i], 0);
const sumXX = afterClosureSqrtTime.reduce((sum, xi) => sum + xi * xi, 0);

const slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
const intercept = (sumY - slope * sumX) / n;

console.log('Linear regression:', {
  slope,
  intercept
});

// Calculate permeability using impulse linear flow equation
// k = (2.2 * μ) / (m * h * φ * ct)
// where m is the slope of p vs sqrt(t)
const permeability = (2.2 * fluidViscosity) / (Math.abs(slope) * thickness * porosity * totalCompressibility);

// Calculate transmissibility (kh/μ)
const transmissibility = permeability * thickness / fluidViscosity;

// Calculate mobility parameter (k/μ)
const mobilityParameter = permeability / fluidViscosity;

// Calculate diffusivity (k / (φ * μ * ct))
const diffusivity = permeability / (porosity * fluidViscosity * totalCompressibility);

// Calculate storage coefficient (φ * ct * h)
const storageCoefficient = porosity * totalCompressibility * thickness;

console.log('Calculated reservoir properties:', {
  permeability,
  transmissibility,
  mobilityParameter,
  diffusivity,
  storageCoefficient
});

console.log('\nAll manual tests completed.');

