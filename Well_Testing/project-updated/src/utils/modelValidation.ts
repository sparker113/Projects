import { Matrix } from 'ml-matrix';
import * as jStat from 'jstat';
import { TestData, FlowRegime } from '../types/wellTest';

/**
 * Validates model fit and calculates statistical measures
 */
export function validateModel(
  observed: number[],
  predicted: number[],
  degreesOfFreedom: number
): {
  r2: number;
  adjustedR2: number;
  rmse: number;
  aic: number;
  bic: number;
  pValue: number;
} {
  const n = observed.length;
  
  // Calculate residuals
  const residuals = observed.map((y, i) => y - predicted[i]);
  
  // Calculate R-squared
  const ssTotal = jStat.sum(observed.map(y => Math.pow(y - jStat.mean(observed), 2)));
  const ssResidual = jStat.sum(residuals.map(r => r * r));
  const r2 = 1 - (ssResidual / ssTotal);
  
  // Calculate adjusted R-squared
  const adjustedR2 = 1 - ((1 - r2) * (n - 1) / (n - degreesOfFreedom - 1));
  
  // Calculate RMSE
  const rmse = Math.sqrt(ssResidual / n);
  
  // Calculate AIC
  const aic = n * Math.log(ssResidual / n) + 2 * degreesOfFreedom;
  
  // Calculate BIC
  const bic = n * Math.log(ssResidual / n) + degreesOfFreedom * Math.log(n);
  
  // Calculate p-value using F-test
  const fStat = (ssTotal - ssResidual) / degreesOfFreedom / (ssResidual / (n - degreesOfFreedom - 1));
  const pValue = 1 - jStat.centralF.cdf(fStat, degreesOfFreedom, n - degreesOfFreedom - 1);
  
  return {
    r2,
    adjustedR2,
    rmse,
    aic,
    bic,
    pValue
  };
}

/**
 * Performs cross-validation of the model
 */
export function crossValidateModel(
  testData: TestData,
  flowRegime: FlowRegime,
  folds: number = 5
): {
  averageR2: number;
  averageRmse: number;
  standardError: number;
} {
  const n = testData.pressure.length;
  const foldSize = Math.floor(n / folds);
  const metrics: { r2: number; rmse: number }[] = [];
  
  for (let i = 0; i < folds; i++) {
    // Split data into training and validation sets
    const validationIndices = Array.from(
      { length: foldSize },
      (_, j) => (i * foldSize + j) % n
    );
    
    const trainingData = {
      pressure: testData.pressure.filter((_, idx) => !validationIndices.includes(idx)),
      time: testData.time.filter((_, idx) => !validationIndices.includes(idx)),
      flowRate: testData.flowRate.filter((_, idx) => !validationIndices.includes(idx)),
      pressureUnit: testData.pressureUnit,
      timeUnit: testData.timeUnit,
      flowRateUnit: testData.flowRateUnit
    };
    
    const validationData = {
      pressure: testData.pressure.filter((_, idx) => validationIndices.includes(idx)),
      time: testData.time.filter((_, idx) => validationIndices.includes(idx)),
      flowRate: testData.flowRate.filter((_, idx) => validationIndices.includes(idx)),
      pressureUnit: testData.pressureUnit,
      timeUnit: testData.timeUnit,
      flowRateUnit: testData.flowRateUnit
    };
    
    // Train model on training data
    const predicted = predictPressures(trainingData, validationData.time, flowRegime);
    
    // Calculate metrics
    const { r2, rmse } = validateModel(validationData.pressure, predicted, 2);
    metrics.push({ r2, rmse });
  }
  
  // Calculate average metrics
  const averageR2 = jStat.mean(metrics.map(m => m.r2));
  const averageRmse = jStat.mean(metrics.map(m => m.rmse));
  const standardError = jStat.stdev(metrics.map(m => m.rmse)) / Math.sqrt(folds);
  
  return {
    averageR2,
    averageRmse,
    standardError
  };
}

/**
 * Predicts pressure values based on flow regime
 */
function predictPressures(
  trainingData: TestData,
  validationTimes: number[],
  flowRegime: FlowRegime
): number[] {
  // Implement prediction logic based on flow regime
  // This is a simplified version - implement full logic based on flow regime equations
  const logTrainingTimes = trainingData.time.map(t => Math.log(t));
  const X = new Matrix([logTrainingTimes]);
  const y = new Matrix([trainingData.pressure]);
  
  // Perform linear regression
  const theta = X.transpose().mmul(X).inverse().mmul(X.transpose()).mmul(y);
  
  // Predict values
  return validationTimes.map(t => 
    theta.get(0, 0) + theta.get(1, 0) * Math.log(t)
  );
}

/**
 * Calculates confidence intervals for model parameters
 */
export function calculateParameterConfidenceIntervals(
  observed: number[],
  predicted: number[],
  parameters: number[],
  alpha: number = 0.05
): Array<[number, number]> {
  const n = observed.length;
  const p = parameters.length;
  
  // Calculate residuals
  const residuals = observed.map((y, i) => y - predicted[i]);
  
  // Calculate standard error
  const mse = jStat.sum(residuals.map(r => r * r)) / (n - p);
  const se = Math.sqrt(mse);
  
  // Calculate t-value
  const tValue = jStat.studentt.inv(1 - alpha / 2, n - p);
  
  // Calculate confidence intervals
  return parameters.map(param => {
    const margin = tValue * se;
    return [param - margin, param + margin];
  });
}