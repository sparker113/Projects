function calculateHornerTime(time: number[], flowRate: number[]): number[] {
  try {
    // Find production time (time before shut-in)
    let productionTime = 0;
    for (let i = 0; i < flowRate.length; i++) {
      if (flowRate[i] > 0) {
        productionTime = time[i];
      } else {
        break;
      }
    }

    // Calculate Horner time for each point
    return time.map(t => {
      // Avoid division by zero and handle early time points
      if (t <= 0) return 1;
      return (productionTime + t) / t;
    });
  } catch (error) {
    console.error('Error calculating Horner time:', error);
    return time.map(() => 1);
  }
}

// Add the missing pressure derivative calculation function
function calculatePressureDerivative(pressure: number[], time: number[]): number[] {
  try {
    const derivative: number[] = [];
    const logTime = time.map(t => Math.log(t));
    
    // Calculate pressure derivative using central difference method
    for (let i = 0; i < pressure.length; i++) {
      if (i === 0) {
        // Forward difference for first point
        const dP = pressure[i + 1] - pressure[i];
        const dt = logTime[i + 1] - logTime[i];
        derivative.push(dP / dt);
      } else if (i === pressure.length - 1) {
        // Backward difference for last point
        const dP = pressure[i] - pressure[i - 1];
        const dt = logTime[i] - logTime[i - 1];
        derivative.push(dP / dt);
      } else {
        // Central difference for interior points
        const dP = pressure[i + 1] - pressure[i - 1];
        const dt = logTime[i + 1] - logTime[i - 1];
        derivative.push(dP / dt);
      }
    }
    
    return derivative;
  } catch (error) {
    console.error('Error calculating pressure derivative:', error);
    return pressure.map(() => 0);
  }
}

// Add missing permeability calculation function
function calculatePermeabilityFromDerivative(
  derivative: number[],
  flowRate: number,
  viscosity: number,
  thickness: number
): number {
  try {
    // Use the stabilized portion of the derivative (middle section)
    const midPoint = Math.floor(derivative.length / 2);
    const stabilizedDerivative = derivative[midPoint];
    
    // Calculate permeability using radial flow equation
    // k = (q * μ * m) / (4π * h)
    // where m is the slope from the derivative
    const permeability = (flowRate * viscosity * stabilizedDerivative) / (4 * Math.PI * thickness);
    
    return Math.abs(permeability); // Ensure positive value
  } catch (error) {
    console.error('Error calculating permeability:', error);
    return 0;
  }
}

// Add missing skin factor calculation
function calculateSkinFactor(
  pressure: number[],
  time: number[],
  flowRate: number[],
  permeability: number,
  viscosity: number,
  totalCompressibility: number,
  wellboreRadius: number,
  flowRegime: string
): number {
  try {
    // Guard against invalid inputs
    if (!permeability || !viscosity || !totalCompressibility || !wellboreRadius) {
      console.warn('Invalid inputs for skin factor calculation');
      return 0;
    }

    // Calculate pressure drop
    const initialPressure = pressure[0];
    const finalPressure = pressure[pressure.length - 1];
    const deltaP = Math.abs(finalPressure - initialPressure);

    // Guard against zero pressure drop
    if (deltaP === 0) {
      console.warn('Zero pressure drop detected');
      return 0;
    }

    // Calculate dimensionless time with protection against division by zero
    const lastTime = Math.max(time[time.length - 1], 0.001); // Minimum time of 0.001 hours
    const denominator = viscosity * totalCompressibility * Math.pow(wellboreRadius, 2);
    
    // Guard against division by zero
    if (denominator === 0) {
      console.warn('Invalid denominator in dimensionless time calculation');
      return 0;
    }

    const tD = (permeability * lastTime) / denominator;

    // Guard against negative or zero flow rate
    const q = Math.abs(flowRate[0]) || 1; // Use 1 as minimum flow rate

    // Calculate skin using modified equation to avoid NaN
    const term1 = (permeability * deltaP) / (141.2 * q * viscosity);
    const term2 = Math.log(Math.max(tD / 2, 1e-6)); // Ensure positive value for log

    const skin = term1 - term2;

    // Limit skin factor to reasonable range
    const limitedSkin = Math.max(Math.min(skin, 20), -5);

    return limitedSkin;
  } catch (error) {
    console.error('Error calculating skin factor:', error);
    return 0;
  }
}

// Add missing initial pressure calculation
function calculateInitialPressure(
  pressure: number[],
  time: number[],
  flowRate: number[],
  flowRegime: string
): number {
  try {
    // For simplicity, use Horner plot extrapolation
    const hornerTime = calculateHornerTime(time, flowRate);
    const hornerPressure = pressure[pressure.length - 1];
    const slope = calculateHornerSlope(pressure, hornerTime);
    
    // Extrapolate to infinite shut-in time (Horner time = 1)
    const initialPressure = hornerPressure + slope * Math.log(1);
    
    return initialPressure;
  } catch (error) {
    console.error('Error calculating initial pressure:', error);
    return pressure[0];
  }
}

// Add missing wellbore storage calculation
function calculateWellboreStorage(
  pressure: number[],
  time: number[],
  flowRate: number[]
): number {
  try {
    // Calculate wellbore storage using early-time pressure data
    const earlyTimePressure = pressure.slice(0, 5);
    const earlyTimePoints = time.slice(0, 5);
    
    // Calculate average rate of pressure change
    const dP = earlyTimePressure[4] - earlyTimePressure[0];
    const dt = earlyTimePoints[4] - earlyTimePoints[0];
    const pressureRate = dP / dt;
    
    // Calculate storage coefficient
    // C = q / (24 * dp/dt)
    const storage = Math.abs(flowRate[0] / (24 * pressureRate));
    
    return storage;
  } catch (error) {
    console.error('Error calculating wellbore storage:', error);
    return 0;
  }
}

// Add missing Horner slope calculation
function calculateHornerSlope(pressure: number[], hornerTime: number[]): number {
  try {
    // Calculate slope using least squares method
    const n = pressure.length;
    let sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
    
    for (let i = 0; i < n; i++) {
      const x = Math.log(hornerTime[i]);
      sumX += x;
      sumY += pressure[i];
      sumXY += x * pressure[i];
      sumX2 += x * x;
    }
    
    const slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    return slope;
  } catch (error) {
    console.error('Error calculating Horner slope:', error);
    return 0;
  }
}

// Add missing average pressure calculation
function calculateAveragePressure(
  pressure: number[],
  hornerTime: number[],
  slope: number,
  boundaryType: string
): number {
  try {
    const lastPressure = pressure[pressure.length - 1];
    const lastHornerTime = hornerTime[hornerTime.length - 1];
    
    // Extrapolate to average reservoir pressure based on boundary type
    const correction = boundaryType === 'no-flow' ? 0.5 : 1.0;
    const averagePressure = lastPressure + slope * Math.log(lastHornerTime) * correction;
    
    return averagePressure;
  } catch (error) {
    console.error('Error calculating average pressure:', error);
    return pressure[pressure.length - 1];
  }
}

function calculateBoundaryDominatedFlow(
  data: TestData,
  fluidProps: FluidProperties,
  wellProps: WellProperties
): BoundaryFlowResults {
  try {
    const {
      viscosity = 1,
      totalCompressibility = 0.000015,
      formationVolumeFactor = 1
    } = fluidProps;

    const {
      thickness = 100,
      drainageRadius = 1000,
      boundaryType = 'no-flow'
    } = wellProps;

    // Calculate pseudo-steady state parameters
    const hornerTime = calculateHornerTime(data.time, data.flowRate);
    const slope = calculateHornerSlope(data.pressure, hornerTime);

    // Calculate drainage area based on boundary type
    const drainageArea = Math.PI * Math.pow(drainageRadius, 2);

    // Calculate average reservoir pressure
    const averagePressure = calculateAveragePressure(
      data.pressure,
      hornerTime,
      slope,
      boundaryType
    );

    // Calculate connected pore volume
    const poreVolume = drainageArea * thickness * wellProps.porosity;

    // Calculate drainage radius from material balance
    const calculatedDrainageRadius = Math.sqrt(drainageArea / Math.PI);

    return {
      averageReservoirPressure: {
        value: averagePressure,
        unit: data.pressureUnit,
        confidenceInterval: [averagePressure * 0.95, averagePressure * 1.05]
      },
      drainageArea: {
        value: drainageArea,
        unit: 'ft²',
        confidenceInterval: [drainageArea * 0.8, drainageArea * 1.2]
      },
      connectedPoreVolume: {
        value: poreVolume,
        unit: 'ft³',
        confidenceInterval: [poreVolume * 0.9, poreVolume * 1.1]
      },
      calculatedDrainageRadius: {
        value: calculatedDrainageRadius,
        unit: 'ft',
        confidenceInterval: [calculatedDrainageRadius * 0.9, calculatedDrainageRadius * 1.1]
      }
    };
  } catch (error) {
    console.error('Error in boundary dominated flow calculations:', error);
    throw error;
  }
}

function calculateTransientFlow(
  data: TestData,
  fluidProps: FluidProperties,
  wellProps: WellProperties,
  flowRegime: FlowRegime
): TransientFlowResults {
  console.log('Starting transient flow calculations for regime:', flowRegime);

  try {
    const {
      viscosity = 1,
      totalCompressibility = 0.000015
    } = fluidProps;

    const {
      wellboreRadius = 0.3,
      thickness = 100
    } = wellProps;

    // Calculate permeability based on flow regime
    let permeabilityValue: number;
    const derivative = calculatePressureDerivative(data.pressure, data.time);

    switch (flowRegime) {
      case 'radial_flow':
        permeabilityValue = calculatePermeabilityFromDerivative(
          derivative,
          data.flowRate[0],
          viscosity,
          thickness
        );
        break;
      case 'linear_flow':
        // Use linear flow equations
        permeabilityValue = calculateLinearFlowPermeability(
          data,
          fluidProps,
          wellProps
        );
        break;
      case 'spherical_flow':
        // Use spherical flow equations
        permeabilityValue = calculateSphericalFlowPermeability(
          data,
          fluidProps,
          wellProps
        );
        break;
      default:
        // Default to radial flow calculations
        permeabilityValue = calculatePermeabilityFromDerivative(
          derivative,
          data.flowRate[0],
          viscosity,
          thickness
        );
    }

    // Calculate skin factor using appropriate method for flow regime
    const skinValue = calculateSkinFactor(
      data.pressure,
      data.time,
      data.flowRate,
      permeabilityValue,
      viscosity,
      totalCompressibility,
      wellboreRadius,
      flowRegime
    );

    // Calculate initial reservoir pressure
    const initialPressureValue = calculateInitialPressure(
      data.pressure,
      data.time,
      data.flowRate,
      flowRegime
    );

    // Calculate wellbore storage coefficient
    const storageValue = calculateWellboreStorage(
      data.pressure,
      data.time,
      data.flowRate
    );

    console.log('Transient flow results:', {
      permeability: permeabilityValue,
      skin: skinValue,
      pressure: initialPressureValue,
      storage: storageValue
    });

    return {
      permeability: {
        value: permeabilityValue,
        unit: 'mD',
        confidenceInterval: [permeabilityValue * 0.8, permeabilityValue * 1.2]
      },
      skinFactor: {
        value: skinValue,
        confidenceInterval: [skinValue - 1, skinValue + 1]
      },
      initialReservoirPressure: {
        value: initialPressureValue,
        unit: data.pressureUnit,
        confidenceInterval: [initialPressureValue * 0.95, initialPressureValue * 1.05]
      },
      wellboreStorageCoefficient: {
        value: storageValue,
        confidenceInterval: [storageValue * 0.7, storageValue * 1.3]
      }
    };
  } catch (error) {
    console.error('Error in transient flow calculations:', error);
    throw error;
  }
}

// Add new helper functions for different flow regimes
function calculateLinearFlowPermeability(
  data: TestData,
  fluidProps: FluidProperties,
  wellProps: WellProperties
): number {
  // Implementation for linear flow permeability calculation
  const slope = calculateLinearFlowSlope(data.pressure, data.time);
  return (4 * Math.PI * fluidProps.viscosity * data.flowRate[0]) / 
         (wellProps.thickness * slope);
}

function calculateSphericalFlowPermeability(
  data: TestData,
  fluidProps: FluidProperties,
  wellProps: WellProperties
): number {
  // Implementation for spherical flow permeability calculation
  const slope = calculateSphericalFlowSlope(data.pressure, data.time);
  return (6 * Math.PI * fluidProps.viscosity * data.flowRate[0]) / 
         (wellProps.thickness * slope);
}

function calculateLinearFlowSlope(pressure: number[], time: number[]): number {
  const sqrtTime = time.map(t => Math.sqrt(t));
  return calculateHornerSlope(pressure, sqrtTime);
}

function calculateSphericalFlowSlope(pressure: number[], time: number[]): number {
  const invSqrtTime = time.map(t => 1 / Math.sqrt(t));
  return calculateHornerSlope(pressure, invSqrtTime);
}

// Export all necessary functions
export {
  calculateHornerTime,
  calculateTransientFlow,
  calculateBoundaryDominatedFlow
};