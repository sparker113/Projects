import React from 'react';
import { DFITResults } from '../types/wellTest';
import { HelpCircle } from 'lucide-react';
import Tooltip from './Tooltip';

interface DFITAnalysisResultsProps {
  results: DFITResults;
}

const DFITAnalysisResults: React.FC<DFITAnalysisResultsProps> = ({ results }) => {
  const formatValue = (value: number, precision: number = 2): string => {
    return value.toFixed(precision);
  };
  
  const getConfidenceText = (confidence: [number, number]): string => {
    return `${formatValue(confidence[0])} - ${formatValue(confidence[1])}`;
  };
  
  return (
    <div className="bg-white shadow-md rounded-lg p-3 md:p-6">
      <h2 className="text-xl font-semibold mb-4">DFIT Analysis Results</h2>
      
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 md:gap-6">
        <div>
          <div className="flex items-center mb-1">
            <h3 className="text-sm font-medium text-gray-700">ISIP</h3>
            <Tooltip content="Instantaneous Shut-In Pressure">
              <HelpCircle size={14} className="ml-1 text-gray-400" />
            </Tooltip>
          </div>
          <div className="bg-gray-50 p-3 rounded border border-gray-200">
            <div className="flex justify-between items-center">
              <span className="text-lg font-bold">{formatValue(results.isip.value)}</span>
              <span className="text-sm text-gray-500">{results.isip.unit}</span>
            </div>
            <div className="text-xs text-gray-500 mt-1">
              95% Confidence: {getConfidenceText(results.isip.confidenceInterval)}
            </div>
          </div>
        </div>
        
        <div>
          <div className="flex items-center mb-1">
            <h3 className="text-sm font-medium text-gray-700">Closure Pressure</h3>
            <Tooltip content="Pressure at which fracture closes">
              <HelpCircle size={14} className="ml-1 text-gray-400" />
            </Tooltip>
          </div>
          <div className="bg-gray-50 p-3 rounded border border-gray-200">
            <div className="flex justify-between items-center">
              <span className="text-lg font-bold">{formatValue(results.closurePressure.value)}</span>
              <span className="text-sm text-gray-500">{results.closurePressure.unit}</span>
            </div>
            <div className="text-xs text-gray-500 mt-1">
              95% Confidence: {getConfidenceText(results.closurePressure.confidenceInterval)}
            </div>
          </div>
        </div>
      </div>
      
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 md:gap-6 mt-4">
        <div>
          <div className="flex items-center mb-1">
            <h3 className="text-sm font-medium text-gray-700">Minimum Stress</h3>
            <Tooltip content="Minimum horizontal stress in formation">
              <HelpCircle size={14} className="ml-1 text-gray-400" />
            </Tooltip>
          </div>
          <div className="bg-gray-50 p-3 rounded border border-gray-200">
            <div className="flex justify-between items-center">
              <span className="text-lg font-bold">{formatValue(results.minStress.value)}</span>
              <span className="text-sm text-gray-500">{results.minStress.unit}</span>
            </div>
            <div className="text-xs text-gray-500 mt-1">
              95% Confidence: {getConfidenceText(results.minStress.confidenceInterval)}
            </div>
          </div>
        </div>
        
        <div>
          <div className="flex items-center mb-1">
            <h3 className="text-sm font-medium text-gray-700">Leak-Off Coefficient</h3>
            <Tooltip content="Measure of fluid loss to formation">
              <HelpCircle size={14} className="ml-1 text-gray-400" />
            </Tooltip>
          </div>
          <div className="bg-gray-50 p-3 rounded border border-gray-200">
            <div className="flex justify-between items-center">
              <span className="text-lg font-bold">{formatValue(results.leakOffCoefficient.value, 6)}</span>
              <span className="text-sm text-gray-500">{results.leakOffCoefficient.unit}</span>
            </div>
            <div className="text-xs text-gray-500 mt-1">
              95% Confidence: {getConfidenceText(results.leakOffCoefficient.confidenceInterval, 6)}
            </div>
          </div>
        </div>
      </div>
      
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 md:gap-6 mt-4">
        <div>
          <div className="flex items-center mb-1">
            <h3 className="text-sm font-medium text-gray-700">Permeability</h3>
            <Tooltip content="Formation permeability from after-closure analysis">
              <HelpCircle size={14} className="ml-1 text-gray-400" />
            </Tooltip>
          </div>
          <div className="bg-gray-50 p-3 rounded border border-gray-200">
            <div className="flex justify-between items-center">
              <span className="text-lg font-bold">{formatValue(results.permeability.value)}</span>
              <span className="text-sm text-gray-500">{results.permeability.unit}</span>
            </div>
            <div className="text-xs text-gray-500 mt-1">
              95% Confidence: {getConfidenceText(results.permeability.confidenceInterval)}
            </div>
          </div>
        </div>
        
        <div>
          <div className="flex items-center mb-1">
            <h3 className="text-sm font-medium text-gray-700">Process Zone Stress</h3>
            <Tooltip content="Stress required to create fracture process zone">
              <HelpCircle size={14} className="ml-1 text-gray-400" />
            </Tooltip>
          </div>
          <div className="bg-gray-50 p-3 rounded border border-gray-200">
            <div className="flex justify-between items-center">
              <span className="text-lg font-bold">{formatValue(results.processZone.value)}</span>
              <span className="text-sm text-gray-500">{results.processZone.unit}</span>
            </div>
            <div className="text-xs text-gray-500 mt-1">
              95% Confidence: {getConfidenceText(results.processZone.confidenceInterval)}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DFITAnalysisResults;