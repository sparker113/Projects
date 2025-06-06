import { TestData } from '../types/wellTest';

/**
 * Time function types for pressure derivative calculations
 */
export type TimeFunction = 
  | 'elapsed_time'       // Regular elapsed time
  | 'log_time'           // Logarithm of elapsed time
  | 'sqrt_time'          // Square root of elapsed time
  | 'squared_time'       // Squared elapsed time
  | 'reciprocal_time'    // 1/time
  | 'superposition_time' // Superposition time function
  | 'horner_time'        // Horner time function
  | 'agarwal_time';      // Agarwal equivalent time

/**
 * Calculates various time functions for pressure derivative analysis
 * @param testData The test data containing time, pressure, and flow rate
 * @param timeFunction The time function to calculate
 * @returns Array of calculated time function values
 */
export function calculateTimeFunction(
  testData: TestData,
  timeFunction: TimeFunction
): number[] {
  const { time, flowRate } = testData;
  
  switch (timeFunction) {
    case 'elapsed_time':
      return time.map(t => t);
      
    case 'log_time':
      return time.map(t => t > 0 ? Math.log10(t) : 0);
      
    case 'sqrt_time':
      return time.map(t => Math.sqrt(Math.max(0, t)));
      
    case 'squared_time':
      return time.map(t => t * t);
      
    case 'reciprocal_time':
      return time.map(t => t > 0 ? 1 / t : 0);
      
    case 'superposition_time':
      // Superposition time function for variable rate tests
      if (!flowRate || flowRate.length === 0) {
        return time.map(t => t); // Default to elapsed time if no flow rate data
      }
      
      const superpositionTime: number[] = [];
      for (let i = 0; i < time.length; i++) {
        let sum = 0;
        for (let j = 0; j < i; j++) {
          const deltaRate = j > 0 ? flowRate[j] - flowRate[j - 1] : flowRate[j];
          const deltaTime = time[i] - time[j];
          if (deltaTime > 0) {
            sum += deltaRate * Math.log(deltaTime);
          }
        }
        superpositionTime.push(sum);
      }
      return superpositionTime;
      
    case 'horner_time':
      // Horner time function for pressure buildup tests
      if (!flowRate || flowRate.length === 0) {
        return time.map(t => t); // Default to elapsed time if no flow rate data
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
      
      return time.map(t => (productionTime + t) / t);
      
    case 'agarwal_time':
      // Agarwal equivalent time function
      if (!flowRate || flowRate.length === 0) {
        return time.map(t => t); // Default to elapsed time if no flow rate data
      }
      
      // Find production time (time before shut-in)
      let prodTime = 0;
      for (let i = 0; i < flowRate.length; i++) {
        if (flowRate[i] > 0) {
          prodTime = time[i];
        } else {
          break;
        }
      }
      
      return time.map(t => {
        const deltaT = t - prodTime;
        if (deltaT <= 0) return t;
        return prodTime * deltaT / (prodTime + deltaT);
      });
      
    default:
      return time.map(t => t); // Default to elapsed time
  }
}

/**
 * Get a human-readable label for a time function
 */
export function getTimeFunctionLabel(timeFunction: TimeFunction): string {
  switch (timeFunction) {
    case 'elapsed_time':
      return 'Elapsed Time';
    case 'log_time':
      return 'Log Time';
    case 'sqrt_time':
      return 'Square Root of Time';
    case 'squared_time':
      return 'Squared Time';
    case 'reciprocal_time':
      return 'Reciprocal Time';
    case 'superposition_time':
      return 'Superposition Time';
    case 'horner_time':
      return 'Horner Time';
    case 'agarwal_time':
      return 'Agarwal Equivalent Time';
    default:
      return 'Time';
  }
}

