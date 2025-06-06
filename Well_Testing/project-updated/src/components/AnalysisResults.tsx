import React, { useState } from 'react';
import { FlowRegime } from '../types/wellTest';
import { HelpCircle } from 'lucide-react';
import Tooltip from './Tooltip';

interface AnalysisResultsProps {
  results: any;
  flowRegime: FlowRegime | null;
  onManualRegimeChange: (regime: FlowRegime) => void;
  defaultFlags: DefaultFlags;
}

const AnalysisResults: React.FC<AnalysisResultsProps> = ({ 
  results, 
  flowRegime,
  onManualRegimeChange,
  defaultFlags
}) => {
  const [activeTab, setActiveTab] = useState<'transient' | 'boundary'>('transient');
  
  const formatValue = (value: number | undefined, precision: number = 2): string => {
    if (value === undefined || isNaN(value)) return '0.00';
    return value.toFixed(precision);
  };
  
  const getConfidenceText = (confidence: [number, number] | undefined): string => {
    if (!confidence || confidence.some(isNaN)) return '0 - 0';
    return `${formatValue(confidence[0])} - ${formatValue(confidence[1])}`;
  };
  
  const formatQualityIndicator = (value: number | undefined): string => {
    const safeValue = value ?? 0;
    if (safeValue >= 0.8) return 'Excellent';
    if (safeValue >= 0.6) return 'Good';
    if (safeValue >= 0.4) return 'Fair';
    return 'Poor';
  };
  
  const getQualityColor = (value: number | undefined): string => {
    const safeValue = value ?? 0;
    if (safeValue >= 0.8) return 'text-green-600';
    if (safeValue >= 0.6) return 'text-blue-600';
    if (safeValue >= 0.4) return 'text-yellow-600';
    return 'text-red-600';
  };

  const renderUncertaintyIndicator = (field: keyof DefaultFlags) => {
    if (defaultFlags[field]) {
      return (
        <Tooltip content="This result is influenced by default input values">
          <span className="text-yellow-500 ml-1">*</span>
        </Tooltip>
      );
    }
    return null;
  };
  
  return (
    <div className="bg-white shadow-md rounded-lg p-3 md:p-6">
      <h2 className="text-xl font-semibold mb-4">Analysis Results</h2>
      
      <div className="mb-4">
        <div className="flex flex-col md:flex-row md:justify-between md:items-center space-y-2 md:space-y-0">
          <div>
            <span className="text-sm font-medium text-gray-700 mr-2">Detected Flow Regime:</span>
            <span className="text-sm font-bold">
              {flowRegime ? flowRegime.replace('_', ' ').charAt(0).toUpperCase() + flowRegime.replace('_', ' ').slice(1) : 'Unknown'}
            </span>
          </div>
          
          <div className="flex items-center">
            <label className="text-sm text-gray-700 mr-2">Override:</label>
            <select 
              value={flowRegime || ''}
              onChange={(e) => onManualRegimeChange(e.target.value as FlowRegime)}
              className="text-sm border-gray-300 rounded"
            >
              <option value="radial_flow">Radial Flow</option>
              <option value="linear_flow">Linear Flow</option>
              <option value="spherical_flow">Spherical Flow</option>
              <option value="boundary_dominated">Boundary Dominated</option>
              <option value="wellbore_storage">Wellbore Storage</option>
              <option value="dual_porosity">Dual Porosity</option>
              <option value="bilinear_flow">Bilinear Flow</option>
              <option value="channel_flow">Channel Flow</option>
              <option value="partial_penetration">Partial Penetration</option>
              <option value="naturally_fractured">Naturally Fractured</option>
              <option value="composite">Composite</option>
            </select>
          </div>
        </div>
      </div>
      
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-gray-50 p-3 rounded border border-gray-200">
          <div className="flex items-center justify-between">
            <span className="text-sm text-gray-500">Regression Quality:</span>
            <span className={`text-sm font-medium ${getQualityColor(results?.qualityIndicators?.regressionQuality)}`}>
              {formatQualityIndicator(results?.qualityIndicators?.regressionQuality)}
            </span>
          </div>
        </div>
        <div className="bg-gray-50 p-3 rounded border border-gray-200">
          <div className="flex items-center justify-between">
            <span className="text-sm text-gray-500">Data Quality:</span>
            <span className={`text-sm font-medium ${getQualityColor(results?.qualityIndicators?.dataQuality)}`}>
              {formatQualityIndicator(results?.qualityIndicators?.dataQuality)}
            </span>
          </div>
        </div>
        <div className="bg-gray-50 p-3 rounded border border-gray-200">
          <div className="flex items-center justify-between">
            <span className="text-sm text-gray-500">Confidence:</span>
            <span className={`text-sm font-medium ${getQualityColor(results?.qualityIndicators?.interpretationConfidence)}`}>
              {formatQualityIndicator(results?.qualityIndicators?.interpretationConfidence)}
            </span>
          </div>
        </div>
      </div>
      
      <div className="mb-4 overflow-x-auto">
        <div className="flex border-b min-w-max">
          <button
            className={`px-4 py-2 ${activeTab === 'transient' ? 'border-b-2 border-blue-700 text-blue-700' : 'text-gray-600'}`}
            onClick={() => setActiveTab('transient')}
          >
            Transient Flow Analysis
          </button>
          <button
            className={`px-4 py-2 ${activeTab === 'boundary' ? 'border-b-2 border-blue-700 text-blue-700' : 'text-gray-600'}`}
            onClick={() => setActiveTab('boundary')}
          >
            Boundary Dominated Flow
          </button>
        </div>
      </div>
      
      {activeTab === 'transient' && results?.transient && (
        <div className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 md:gap-6">
            <div>
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">
                  Permeability
                  {renderUncertaintyIndicator('porosity')}
                  {renderUncertaintyIndicator('compressibility')}
                </h3>
                <Tooltip content="Calculated reservoir permeability based on transient flow analysis">
                  <HelpCircle size={14} className="ml-1 text-gray-400" />
                </Tooltip>
              </div>
              <div className="bg-gray-50 p-3 rounded border border-gray-200">
                <div className="flex justify-between items-center">
                  <span className="text-lg font-bold">{formatValue(results.transient?.permeability?.value)}</span>
                  <span className="text-sm text-gray-500">{results.transient?.permeability?.unit}</span>
                </div>
                <div className="text-xs text-gray-500 mt-1">
                  95% Confidence: {getConfidenceText(results.transient?.permeability?.confidenceInterval)}
                </div>
              </div>
            </div>
            
            <div>
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Skin Factor</h3>
                <Tooltip content="Dimensionless skin factor indicating well damage or stimulation">
                  <HelpCircle size={14} className="ml-1 text-gray-400" />
                </Tooltip>
              </div>
              <div className="bg-gray-50 p-3 rounded border border-gray-200">
                <div className="flex justify-between items-center">
                  <span className="text-lg font-bold">{formatValue(results.transient?.skinFactor?.value, 1)}</span>
                  <span className="text-sm text-gray-500">dimensionless</span>
                </div>
                <div className="text-xs text-gray-500 mt-1">
                  95% Confidence: {getConfidenceText(results.transient?.skinFactor?.confidenceInterval)}
                </div>
              </div>
            </div>
          </div>
          
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 md:gap-6">
            <div>
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Initial Reservoir Pressure</h3>
                <Tooltip content="Extrapolated initial reservoir pressure">
                  <HelpCircle size={14} className="ml-1 text-gray-400" />
                </Tooltip>
              </div>
              <div className="bg-gray-50 p-3 rounded border border-gray-200">
                <div className="flex justify-between items-center">
                  <span className="text-lg font-bold">{formatValue(results.transient?.initialReservoirPressure?.value)}</span>
                  <span className="text-sm text-gray-500">{results.transient?.initialReservoirPressure?.unit}</span>
                </div>
                <div className="text-xs text-gray-500 mt-1">
                  95% Confidence: {getConfidenceText(results.transient?.initialReservoirPressure?.confidenceInterval)}
                </div>
              </div>
            </div>
            
            <div>
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Wellbore Storage Coefficient</h3>
                <Tooltip content="Coefficient describing wellbore storage effects">
                  <HelpCircle size={14} className="ml-1 text-gray-400" />
                </Tooltip>
              </div>
              <div className="bg-gray-50 p-3 rounded border border-gray-200">
                <div className="flex justify-between items-center">
                  <span className="text-lg font-bold">{formatValue(results.transient?.wellboreStorageCoefficient?.value, 6)}</span>
                  <span className="text-sm text-gray-500">bbl/psi</span>
                </div>
                <div className="text-xs text-gray-500 mt-1">
                  95% Confidence: {getConfidenceText(results.transient?.wellboreStorageCoefficient?.confidenceInterval)}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
      
      {activeTab === 'boundary' && results?.boundary && (
        <div className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 md:gap-6">
            <div>
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Calculated Drainage Radius</h3>
                <Tooltip content="Calculated radius of investigation">
                  <HelpCircle size={14} className="ml-1 text-gray-400" />
                </Tooltip>
              </div>
              <div className="bg-gray-50 p-3 rounded border border-gray-200">
                <div className="flex justify-between items-center">
                  <span className="text-lg font-bold">
                    {formatValue(results.boundary?.calculatedDrainageRadius?.value)}
                  </span>
                  <span className="text-sm text-gray-500">
                    {results.boundary?.calculatedDrainageRadius?.unit}
                  </span>
                </div>
                <div className="text-xs text-gray-500 mt-1">
                  95% Confidence: {getConfidenceText(results.boundary?.calculatedDrainageRadius?.confidenceInterval)}
                </div>
              </div>
            </div>
            
            <div>
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Connected Pore Volume</h3>
                <Tooltip content="Total connected pore volume">
                  <HelpCircle size={14} className="ml-1 text-gray-400" />
                </Tooltip>
              </div>
              <div className="bg-gray-50 p-3 rounded border border-gray-200">
                <div className="flex justify-between items-center">
                  <span className="text-lg font-bold">
                    {formatValue(results.boundary?.connectedPoreVolume?.value)}
                  </span>
                  <span className="text-sm text-gray-500">
                    {results.boundary?.connectedPoreVolume?.unit}
                  </span>
                </div>
                <div className="text-xs text-gray-500 mt-1">
                  95% Confidence: {getConfidenceText(results.boundary?.connectedPoreVolume?.confidenceInterval)}
                </div>
              </div>
            </div>
          </div>
          
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 md:gap-6">
            <div>
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Average Reservoir Pressure</h3>
                <Tooltip content="Current average pressure in the drainage area">
                  <HelpCircle size={14} className="ml-1 text-gray-400" />
                </Tooltip>
              </div>
              <div className="bg-gray-50 p-3 rounded border border-gray-200">
                <div className="flex justify-between items-center">
                  <span className="text-lg font-bold">{formatValue(results.boundary?.averageReservoirPressure?.value)}</span>
                  <span className="text-sm text-gray-500">{results.boundary?.averageReservoirPressure?.unit}</span>
                </div>
                <div className="text-xs text-gray-500 mt-1">
                  95% Confidence: {getConfidenceText(results.boundary?.averageReservoirPressure?.confidenceInterval)}
                </div>
              </div>
            </div>
            
            <div>
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Drainage Area</h3>
                <Tooltip content="Area being drained by the well">
                  <HelpCircle size={14} className="ml-1 text-gray-400" />
                </Tooltip>
              </div>
              <div className="bg-gray-50 p-3 rounded border border-gray-200">
                <div className="flex justify-between items-center">
                  <span className="text-lg font-bold">{formatValue(results.boundary?.drainageArea?.value)}</span>
                  <span className="text-sm text-gray-500">{results.boundary?.drainageArea?.unit}</span>
                </div>
                <div className="text-xs text-gray-500 mt-1">
                  95% Confidence: {getConfidenceText(results.boundary?.drainageArea?.confidenceInterval)}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {defaultFlags && Object.values(defaultFlags).some(flag => flag) && (
        <div className="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded-md">
          <p className="text-sm text-yellow-700">
            * Results marked with an asterisk are influenced by default input values. 
            Consider updating these inputs for more accurate results.
          </p>
        </div>
      )}
    </div>
  );
};

export default AnalysisResults;