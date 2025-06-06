import { TestData } from '../types/wellTest';
import { calculatePressureDerivative, smoothDerivative } from './flowRegimeIdentification';
import { TimeFunction, calculateTimeFunction } from './timeFunctions';

export type PlotType = 
  | 'log-log'           // Log-log diagnostic plot
  | 'horner'            // Horner plot
  | 'mdh'               // MDH plot
  | 'g-function'        // G-function plot
  | 'pressure'          // Raw pressure plot
  | 'pressure-derivative' // Pressure derivative plot
  | 'custom';           // Custom plot with selectable time function

export interface PlotData {
  name: string;
  time: number;
  pressure: number;
  derivative?: number;
  gFunction?: number;
  primaryDerivative?: number; // dP (primary pressure derivative)
  rawPressure?: number;       // Raw pressure value
}

/**
 * Creates data for diagnostic plots with various time functions
 */
export function createPlotData(
  testData: TestData, 
  plotType: PlotType,
  timeFunction: TimeFunction = 'elapsed_time'
): PlotData[] {
  switch (plotType) {
    case 'log-log':
      return createLogLogPlot(testData);
    case 'horner':
      return createHornerPlot(testData);
    case 'mdh':
      return createMDHPlot(testData);
    case 'g-function':
      return createGFunctionPlot(testData);
    case 'pressure':
      return createPressurePlot(testData, timeFunction);
    case 'pressure-derivative':
      return createPressureDerivativePlot(testData, timeFunction);
    case 'custom':
      return createCustomPlot(testData, timeFunction);
    default:
      return createLogLogPlot(testData);
  }
}

/**
 * Creates data for log-log diagnostic plot
 */
export function createLogLogPlot(testData: TestData): PlotData[] {
  const { time, pressure } = testData;
  
  // Filter out invalid values for log calculations
  const validData = time.map((t, i) => ({
    time: t,
    pressure: pressure[i]
  })).filter(d => d.time > 0 && d.pressure > 0);

  // Calculate pressure derivative
  const derivative = calculatePressureDerivative(
    validData.map(d => d.pressure),
    validData.map(d => d.time)
  );
  const smoothedDerivative = smoothDerivative(derivative);

  // Create plot data with log transformations
  return validData.map((d, i) => ({
    name: d.time.toString(),
    time: Math.log10(d.time),
    pressure: Math.log10(d.pressure),
    derivative: smoothedDerivative[i] > 0 ? Math.log10(smoothedDerivative[i]) : null,
    rawPressure: d.pressure
  }));
}

/**
 * Creates data for Horner plot
 */
export function createHornerPlot(testData: TestData): PlotData[] {
  const { time, pressure, flowRate } = testData;
  
  // If flow rate data is not available (DFIT), return empty array
  if (!flowRate || flowRate.length === 0) {
    return [];
  }
  
  // Find production time (time before shut-in)
  let productionTime = 0;
  for (let i = 0; i < flowRate.length; i++) {
    if (flowRate[i] > 0) {
      productionTime = time[i];
    } else {
      break;
    }
  }

  // Calculate Horner time for each point after shut-in
  return time.map((t, i) => {
    if (flowRate[i] === 0 && t > productionTime) {
      const hornerTime = (productionTime + t) / t;
      return {
        name: t.toString(),
        time: hornerTime,
        pressure: pressure[i],
        rawPressure: pressure[i]
      };
    }
    return null;
  }).filter(Boolean);
}

/**
 * Creates data for MDH (Miller-Dyes-Hutchinson) plot
 */
export function createMDHPlot(testData: TestData): PlotData[] {
  const { time, pressure } = testData;
  
  // Filter out invalid values for log calculations
  return time.map((t, i) => {
    if (t > 0) {
      return {
        name: t.toString(),
        time: Math.log10(t),
        pressure: pressure[i],
        rawPressure: pressure[i]
      };
    }
    return null;
  }).filter(Boolean);
}

/**
 * Creates data for G-function plot
 */
export function createGFunctionPlot(testData: TestData): PlotData[] {
  const { time, pressure, flowRate } = testData;
  
  // For DFIT without flow rate data, assume the first point is the shut-in time
  let shutInIndex = 0;
  
  // If the first time is 0, use the second time point as shut-in time
  if (time[0] === 0) {
    shutInIndex = 1;
  }
  
  let shutInTime = time[shutInIndex];
  let shutInPressure = pressure[shutInIndex];
  
  // If flow rate data is available, find the shut-in time
  if (flowRate && flowRate.length > 0) {
    const flowShutInIndex = flowRate.findIndex(r => r === 0);
    if (flowShutInIndex !== -1) {
      shutInIndex = flowShutInIndex;
      shutInTime = time[shutInIndex];
      shutInPressure = pressure[shutInIndex];
    }
  }

  // Calculate G-function values
  return time.slice(shutInIndex).map((t, i) => {
    const deltaT = t - shutInTime;
    if (deltaT <= 0) return null;

    // Calculate g-function using standard formula
    const g = Math.sqrt(deltaT / shutInTime);
    
    // Calculate derivative if not at endpoints
    let derivative = null;
    if (i > 0 && i < time.length - shutInIndex - 1) {
      const dp = pressure[shutInIndex + i + 1] - pressure[shutInIndex + i - 1];
      const dg = g * 2; // Approximate derivative window
      derivative = dp / dg;
    }

    return {
      name: t.toString(),
      time: g,
      pressure: pressure[shutInIndex + i] - shutInPressure,
      derivative,
      rawPressure: pressure[shutInIndex + i]
    };
  }).filter(Boolean);
}

/**
 * Creates data for raw pressure plot with custom time function
 */
export function createPressurePlot(
  testData: TestData,
  timeFunction: TimeFunction = 'elapsed_time'
): PlotData[] {
  const { time, pressure } = testData;
  
  // Calculate the selected time function
  const transformedTime = calculateTimeFunction(testData, timeFunction);
  
  // Create plot data
  return time.map((t, i) => ({
    name: t.toString(),
    time: transformedTime[i],
    pressure: pressure[i],
    rawPressure: pressure[i]
  }));
}

/**
 * Creates data for pressure derivative plot with custom time function
 */
export function createPressureDerivativePlot(
  testData: TestData,
  timeFunction: TimeFunction = 'elapsed_time'
): PlotData[] {
  const { time, pressure } = testData;
  
  // Calculate the selected time function
  const transformedTime = calculateTimeFunction(testData, timeFunction);
  
  // Calculate pressure derivative
  const derivative = calculatePressureDerivative(pressure, time);
  const smoothedDerivative = smoothDerivative(derivative);
  
  // Calculate primary pressure derivative (dP)
  const primaryDerivative: number[] = [];
  for (let i = 1; i < pressure.length - 1; i++) {
    const dp = pressure[i + 1] - pressure[i - 1];
    const dt = time[i + 1] - time[i - 1];
    primaryDerivative.push(dt > 0 ? dp / dt : 0);
  }
  // Handle endpoints
  primaryDerivative.unshift(primaryDerivative[0] || 0);
  primaryDerivative.push(primaryDerivative[primaryDerivative.length - 1] || 0);
  
  // Create plot data
  return time.map((t, i) => ({
    name: t.toString(),
    time: transformedTime[i],
    pressure: pressure[i],
    derivative: smoothedDerivative[i],
    primaryDerivative: primaryDerivative[i],
    rawPressure: pressure[i]
  }));
}

/**
 * Creates data for custom plot with selectable time function
 */
export function createCustomPlot(
  testData: TestData,
  timeFunction: TimeFunction = 'elapsed_time'
): PlotData[] {
  const { time, pressure } = testData;
  
  // Calculate the selected time function
  const transformedTime = calculateTimeFunction(testData, timeFunction);
  
  // Calculate pressure derivative
  const derivative = calculatePressureDerivative(pressure, time);
  const smoothedDerivative = smoothDerivative(derivative);
  
  // Calculate primary pressure derivative (dP)
  const primaryDerivative: number[] = [];
  for (let i = 1; i < pressure.length - 1; i++) {
    const dp = pressure[i + 1] - pressure[i - 1];
    const dt = time[i + 1] - time[i - 1];
    primaryDerivative.push(dt > 0 ? dp / dt : 0);
  }
  // Handle endpoints
  primaryDerivative.unshift(primaryDerivative[0] || 0);
  primaryDerivative.push(primaryDerivative[primaryDerivative.length - 1] || 0);
  
  // Create plot data
  return time.map((t, i) => ({
    name: t.toString(),
    time: transformedTime[i],
    pressure: pressure[i],
    derivative: smoothedDerivative[i],
    primaryDerivative: primaryDerivative[i],
    rawPressure: pressure[i]
  }));
}

/**
 * Formats and exports plot data
 */
export function exportPlotData(testData: TestData, format: 'csv' | 'json' = 'csv'): string {
  const { time, pressure, flowRate } = testData;
  const derivative = calculatePressureDerivative(pressure, time);
  
  if (format === 'csv') {
    // Handle case when flow rate data is not available (DFIT)
    if (flowRate) {
      let csv = 'Time,Pressure,FlowRate,Derivative\n';
      time.forEach((t, i) => {
        csv += `${t},${pressure[i]},${flowRate[i]},${derivative[i]}\n`;
      });
      return csv;
    } else {
      let csv = 'Time,Pressure,Derivative\n';
      time.forEach((t, i) => {
        csv += `${t},${pressure[i]},${derivative[i]}\n`;
      });
      return csv;
    }
  }
  
  // For JSON format
  const result: any = {
    time,
    pressure,
    derivative
  };
  
  // Add flow rate if available
  if (flowRate) {
    result.flowRate = flowRate;
  }
  
  return JSON.stringify(result, null, 2);
}

