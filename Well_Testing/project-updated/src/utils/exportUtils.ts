import { TestData, FluidProperties, WellProperties, AnalysisResults } from '../types/wellTest';

interface ExportData {
  testData: TestData;
  fluidProperties: FluidProperties;
  wellProperties: WellProperties;
  results: AnalysisResults;
}

/**
 * Formats analysis results into CSV format
 */
function formatAsCSV(data: ExportData): string {
  const { testData, fluidProperties, wellProperties, results } = data;
  
  let csv = '';
  
  // Test Information
  csv += 'Well Test Analysis Results\n\n';
  csv += `Analysis Date,${new Date().toISOString()}\n`;
  csv += `Flow Regime,${results.flowRegime.replace('_', ' ')}\n\n`;
  
  // Quality Indicators
  csv += 'Quality Indicators\n';
  csv += `Regression Quality,${results.qualityIndicators.regressionQuality.toFixed(3)}\n`;
  csv += `Data Quality,${results.qualityIndicators.dataQuality.toFixed(3)}\n`;
  csv += `Interpretation Confidence,${results.qualityIndicators.interpretationConfidence.toFixed(3)}\n\n`;
  
  // Transient Flow Results
  csv += 'Transient Flow Analysis\n';
  csv += `Parameter,Value,Unit,Confidence Interval\n`;
  csv += `Permeability,${results.transient.permeability.value.toFixed(2)},${results.transient.permeability.unit},${results.transient.permeability.confidenceInterval[0].toFixed(2)}-${results.transient.permeability.confidenceInterval[1].toFixed(2)}\n`;
  csv += `Skin Factor,${results.transient.skinFactor.value.toFixed(2)},dimensionless,${results.transient.skinFactor.confidenceInterval[0].toFixed(2)}-${results.transient.skinFactor.confidenceInterval[1].toFixed(2)}\n`;
  csv += `Initial Reservoir Pressure,${results.transient.initialReservoirPressure.value.toFixed(2)},${results.transient.initialReservoirPressure.unit},${results.transient.initialReservoirPressure.confidenceInterval[0].toFixed(2)}-${results.transient.initialReservoirPressure.confidenceInterval[1].toFixed(2)}\n`;
  csv += `Wellbore Storage Coefficient,${results.transient.wellboreStorageCoefficient.value.toFixed(6)},bbl/psi,${results.transient.wellboreStorageCoefficient.confidenceInterval[0].toFixed(6)}-${results.transient.wellboreStorageCoefficient.confidenceInterval[1].toFixed(6)}\n\n`;
  
  // Boundary Dominated Flow Results
  csv += 'Boundary Dominated Flow Analysis\n';
  csv += `Parameter,Value,Unit,Confidence Interval\n`;
  csv += `Distance to Boundaries,${results.boundary.distanceToBoundaries.value.toFixed(2)},${results.boundary.distanceToBoundaries.unit},${results.boundary.distanceToBoundaries.confidenceInterval[0].toFixed(2)}-${results.boundary.distanceToBoundaries.confidenceInterval[1].toFixed(2)}\n`;
  csv += `Connected Reservoir Volume,${results.boundary.connectedReservoirVolume.value.toFixed(2)},${results.boundary.connectedReservoirVolume.unit},${results.boundary.connectedReservoirVolume.confidenceInterval[0].toFixed(2)}-${results.boundary.connectedReservoirVolume.confidenceInterval[1].toFixed(2)}\n`;
  csv += `Average Reservoir Pressure,${results.boundary.averageReservoirPressure.value.toFixed(2)},${results.boundary.averageReservoirPressure.unit},${results.boundary.averageReservoirPressure.confidenceInterval[0].toFixed(2)}-${results.boundary.averageReservoirPressure.confidenceInterval[1].toFixed(2)}\n`;
  csv += `Drainage Area,${results.boundary.drainageArea.value.toFixed(2)},${results.boundary.drainageArea.unit},${results.boundary.drainageArea.confidenceInterval[0].toFixed(2)}-${results.boundary.drainageArea.confidenceInterval[1].toFixed(2)}\n\n`;
  
  // Input Data
  csv += 'Raw Data\n';
  csv += `Time (${testData.timeUnit}),Pressure (${testData.pressureUnit}),Flow Rate (${testData.flowRateUnit})\n`;
  testData.time.forEach((t, i) => {
    csv += `${t},${testData.pressure[i]},${testData.flowRate[i]}\n`;
  });
  
  return csv;
}

/**
 * Exports analysis results as a CSV file
 */
export function exportAnalysisResults(
  testData: TestData,
  fluidProperties: FluidProperties,
  wellProperties: WellProperties,
  results: AnalysisResults
): void {
  const data = {
    testData,
    fluidProperties,
    wellProperties,
    results
  };
  
  const csv = formatAsCSV(data);
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  
  link.setAttribute('href', url);
  link.setAttribute('download', `well_test_analysis_${new Date().toISOString().split('T')[0]}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}