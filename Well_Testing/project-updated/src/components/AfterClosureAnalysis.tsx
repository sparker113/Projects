import React, { useState, useEffect } from 'react';
import { TestData } from '../types/wellTest';
import { 
  ClosureAnalysisMethod, 
  ClosurePoint,
  detectClosure
} from '../utils/closureAnalysis';
import {
  AfterClosureFlowRegime,
  AfterClosureAnalysisResult,
  performAfterClosureAnalysis
} from '../utils/afterClosureAnalysis';
import { createPlotData, PlotData } from '../utils/plotUtils';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ReferenceLine,
  ReferenceArea
} from 'recharts';
import { HelpCircle, ChevronDown, ChevronUp, AlertCircle } from 'lucide-react';

interface AfterClosureAnalysisProps {
  testData: TestData;
  closurePoint?: ClosurePoint;
  fluidViscosity?: number;
  totalCompressibility?: number;
  porosity?: number;
  thickness?: number;
}

const AfterClosureAnalysis: React.FC<AfterClosureAnalysisProps> = ({ 
  testData,
  closurePoint: propClosurePoint,
  fluidViscosity = 1,
  totalCompressibility = 1e-5,
  porosity = 0.1,
  thickness = 100
}) => {
  // State for analysis configuration
  const [selectedFlowRegime, setSelectedFlowRegime] = useState<AfterClosureFlowRegime>('unknown');
  const [showAdvancedOptions, setShowAdvancedOptions] = useState(false);
  
  // State for analysis results
  const [closurePoint, setClosurePoint] = useState<ClosurePoint | null>(null);
  const [analysisResult, setAnalysisResult] = useState<AfterClosureAnalysisResult | null>(null);
  const [plotData, setPlotData] = useState<PlotData[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  
  // Initialize closure point if not provided
  useEffect(() => {
    if (!closurePoint && testData) {
      if (propClosurePoint) {
        setClosurePoint(propClosurePoint);
      } else {
        // Auto-detect closure point using G-function method
        const detectedClosure = detectClosure(testData, 'g-function');
        setClosurePoint(detectedClosure);
      }
    }
  }, [testData, propClosurePoint]);
  
  // Run after-closure analysis when closure point changes
  useEffect(() => {
    if (testData && closurePoint) {
      setIsLoading(true);
      
      try {
        // Perform after-closure analysis
        const result = performAfterClosureAnalysis(
          testData,
          closurePoint,
          fluidViscosity,
          totalCompressibility,
          porosity,
          thickness
        );
        
        setAnalysisResult(result);
        setSelectedFlowRegime(result.flowRegime);
        
        // Generate plot data based on flow regime
        const data = createAfterClosurePlotData(testData, closurePoint, result.flowRegime);
        setPlotData(data);
      } catch (error) {
        console.error('Error in after-closure analysis:', error);
      }
      
      setIsLoading(false);
    }
  }, [testData, closurePoint, fluidViscosity, totalCompressibility, porosity, thickness]);
  
  // Create after-closure plot data
  const createAfterClosurePlotData = (
    data: TestData,
    closure: ClosurePoint,
    flowRegime: AfterClosureFlowRegime
  ): PlotData[] => {
    const { time, pressure } = data;
    
    // Get data after closure
    const afterClosureTime = time.slice(closure.index);
    const afterClosurePressure = pressure.slice(closure.index);
    
    // Calculate pressure difference from closure
    const deltaPressure = afterClosurePressure.map(p => closure.pressure - p);
    
    if (flowRegime === 'impulse_linear') {
      // For linear flow, plot delta-p vs sqrt(t)
      const sqrtTime = afterClosureTime.map(t => Math.sqrt(t));
      
      return afterClosureTime.map((t, i) => ({
        name: t.toString(),
        time: sqrtTime[i],
        pressure: deltaPressure[i],
        rawPressure: afterClosurePressure[i]
      }));
    } else if (flowRegime === 'impulse_radial') {
      // For radial flow, plot delta-p vs log(t)
      const logTime = afterClosureTime.map(t => Math.log10(Math.max(t, 1e-10)));
      
      return afterClosureTime.map((t, i) => ({
        name: t.toString(),
        time: logTime[i],
        pressure: deltaPressure[i],
        rawPressure: afterClosurePressure[i]
      }));
    } else {
      // Default: plot delta-p vs t
      return afterClosureTime.map((t, i) => ({
        name: t.toString(),
        time: t,
        pressure: deltaPressure[i],
        rawPressure: afterClosurePressure[i]
      }));
    }
  };
  
  // Format axis tick values
  const formatAxisTick = (value: number) => {
    if (Math.abs(value) < 0.01 || Math.abs(value) > 9999) {
      return value.toExponential(2);
    }
    return value.toFixed(2);
  };
  
  // Get axis labels based on flow regime
  const getAxisLabel = () => {
    switch (selectedFlowRegime) {
      case 'impulse_linear':
        return {
          x: 'Square Root of Time',
          y: `Delta Pressure (${testData.pressureUnit})`
        };
      case 'impulse_radial':
        return {
          x: 'Log Time',
          y: `Delta Pressure (${testData.pressureUnit})`
        };
      default:
        return {
          x: 'Time',
          y: `Delta Pressure (${testData.pressureUnit})`
        };
    }
  };
  
  // Get flow regime description
  const getFlowRegimeDescription = (regime: AfterClosureFlowRegime): string => {
    switch (regime) {
      case 'impulse_linear':
        return 'Impulse linear flow: -1/2 slope on log-log plot, indicating flow in a linear system';
      case 'impulse_radial':
        return 'Impulse radial flow: -1 slope on log-log plot, indicating flow in a radial system';
      default:
        return 'Flow regime not identified or ambiguous';
    }
  };
  
  // Handle flow regime selection
  const handleFlowRegimeChange = (regime: AfterClosureFlowRegime) => {
    setSelectedFlowRegime(regime);
    
    // Update plot data for the selected flow regime
    if (testData && closurePoint) {
      const data = createAfterClosurePlotData(testData, closurePoint, regime);
      setPlotData(data);
    }
  };
  
  return (
    <div className="space-y-6">
      <div className="bg-white shadow-md rounded-lg p-4">
        <h2 className="text-xl font-semibold mb-4">After-Closure Analysis</h2>
        
        {/* Flow regime selection */}
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Flow Regime
          </label>
          <div className="flex flex-wrap gap-2">
            <button
              className={`px-3 py-1.5 text-sm font-medium rounded-lg ${
                selectedFlowRegime === 'impulse_linear'
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
              onClick={() => handleFlowRegimeChange('impulse_linear')}
            >
              Impulse Linear Flow
            </button>
            <button
              className={`px-3 py-1.5 text-sm font-medium rounded-lg ${
                selectedFlowRegime === 'impulse_radial'
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
              onClick={() => handleFlowRegimeChange('impulse_radial')}
            >
              Impulse Radial Flow
            </button>
          </div>
          
          {analysisResult && analysisResult.flowRegime !== 'unknown' && analysisResult.flowRegime !== selectedFlowRegime && (
            <div className="mt-2 p-2 bg-yellow-50 border border-yellow-200 rounded-md flex items-start">
              <AlertCircle size={16} className="text-yellow-500 mt-0.5 mr-2 flex-shrink-0" />
              <p className="text-sm text-yellow-700">
                Auto-detected flow regime ({analysisResult.flowRegime.replace('_', ' ')}) differs from selected regime.
                Confidence: {(analysisResult.confidence * 100).toFixed(0)}%
              </p>
            </div>
          )}
        </div>
        
        {/* Advanced options */}
        <div className="mb-4">
          <button
            type="button"
            onClick={() => setShowAdvancedOptions(!showAdvancedOptions)}
            className="flex items-center text-sm text-gray-600 hover:text-gray-900"
          >
            {showAdvancedOptions ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
            <span className="ml-1">Advanced Options</span>
          </button>
          
          {showAdvancedOptions && (
            <div className="mt-3 p-3 bg-gray-50 rounded-lg">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Fluid Viscosity (cp)
                  </label>
                  <input
                    type="number"
                    value={fluidViscosity}
                    onChange={(e) => {
                      const value = parseFloat(e.target.value);
                      if (!isNaN(value) && value > 0) {
                        // Update via props or state management in a real app
                      }
                    }}
                    className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    min="0.001"
                    step="0.1"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Total Compressibility (1/psi)
                  </label>
                  <input
                    type="number"
                    value={totalCompressibility}
                    onChange={(e) => {
                      const value = parseFloat(e.target.value);
                      if (!isNaN(value) && value > 0) {
                        // Update via props or state management in a real app
                      }
                    }}
                    className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    min="0.000001"
                    step="0.000001"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Porosity (fraction)
                  </label>
                  <input
                    type="number"
                    value={porosity}
                    onChange={(e) => {
                      const value = parseFloat(e.target.value);
                      if (!isNaN(value) && value > 0 && value < 1) {
                        // Update via props or state management in a real app
                      }
                    }}
                    className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    min="0.001"
                    max="0.999"
                    step="0.01"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Thickness (ft)
                  </label>
                  <input
                    type="number"
                    value={thickness}
                    onChange={(e) => {
                      const value = parseFloat(e.target.value);
                      if (!isNaN(value) && value > 0) {
                        // Update via props or state management in a real app
                      }
                    }}
                    className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    min="1"
                    step="1"
                  />
                </div>
              </div>
            </div>
          )}
        </div>
        
        {/* Flow regime indicator */}
        <div className="border border-gray-300 bg-white rounded-lg p-3 mb-3 shadow-soft">
          <div className="flex items-center">
            <div className={`w-3 h-3 rounded-full ${selectedFlowRegime !== 'unknown' ? 'bg-green-600' : 'bg-yellow-600'} mr-2`}></div>
            <p className="text-sm text-gray-800">
              {getFlowRegimeDescription(selectedFlowRegime)}
            </p>
          </div>
        </div>
        
        {/* Plot area */}
        <div className="bg-gray-50 rounded-lg border border-gray-200 p-4 h-[400px] relative">
          {isLoading ? (
            <div className="flex items-center justify-center h-full">
              <div className="text-center">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent mb-2"></div>
                <p className="text-gray-700">Analyzing after-closure data...</p>
              </div>
            </div>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <LineChart
                data={plotData}
                margin={{ top: 20, right: 30, left: 20, bottom: 20 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis
                  dataKey="time"
                  type="number"
                  tickFormatter={formatAxisTick}
                  label={{
                    value: getAxisLabel().x,
                    position: 'bottom',
                    offset: 0,
                    style: { fill: '#374151', fontSize: '14px' }
                  }}
                  tick={{ fill: '#374151', fontSize: '12px' }}
                />
                <YAxis
                  type="number"
                  tickFormatter={formatAxisTick}
                  label={{
                    value: getAxisLabel().y,
                    angle: -90,
                    position: 'left',
                    offset: 0,
                    style: { fill: '#374151', fontSize: '14px' }
                  }}
                  tick={{ fill: '#374151', fontSize: '12px' }}
                />
                <Tooltip
                  formatter={(value: number) => formatAxisTick(value)}
                  contentStyle={{
                    backgroundColor: 'white',
                    border: '1px solid #e5e7eb',
                    borderRadius: '0.375rem',
                    fontSize: '12px'
                  }}
                />
                <Legend
                  verticalAlign="top"
                  height={36}
                  wrapperStyle={{
                    fontSize: '14px',
                    paddingTop: '8px'
                  }}
                />
                
                {/* Delta pressure line */}
                <Line
                  type="monotone"
                  dataKey="pressure"
                  stroke="#4338ca"
                  name="Delta Pressure"
                  dot={false}
                  strokeWidth={2}
                />
                
                {/* Trend line */}
                {analysisResult && (
                  <Line
                    type="monotone"
                    dataKey="trendLine"
                    stroke="#ef4444"
                    name="Trend Line"
                    dot={false}
                    strokeWidth={2}
                    strokeDasharray="5 5"
                  />
                )}
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
      
      {/* Analysis results */}
      {analysisResult && (
        <div className="bg-white shadow-md rounded-lg p-4">
          <h2 className="text-xl font-semibold mb-4">After-Closure Analysis Results</h2>
          
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            <div className="bg-gray-50 p-3 rounded border border-gray-200">
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Permeability</h3>
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </div>
              <div className="flex justify-between items-center">
                <span className="text-lg font-bold">{formatAxisTick(analysisResult.permeability)}</span>
                <span className="text-sm text-gray-500">md</span>
              </div>
            </div>
            
            <div className="bg-gray-50 p-3 rounded border border-gray-200">
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Transmissibility</h3>
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </div>
              <div className="flex justify-between items-center">
                <span className="text-lg font-bold">{formatAxisTick(analysisResult.transmissibility)}</span>
                <span className="text-sm text-gray-500">md-ft/cp</span>
              </div>
            </div>
            
            <div className="bg-gray-50 p-3 rounded border border-gray-200">
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Mobility Parameter</h3>
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </div>
              <div className="flex justify-between items-center">
                <span className="text-lg font-bold">{formatAxisTick(analysisResult.mobilityParameter)}</span>
                <span className="text-sm text-gray-500">md/cp</span>
              </div>
            </div>
            
            <div className="bg-gray-50 p-3 rounded border border-gray-200">
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Diffusivity</h3>
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </div>
              <div className="flex justify-between items-center">
                <span className="text-lg font-bold">{formatAxisTick(analysisResult.diffusivity)}</span>
                <span className="text-sm text-gray-500">ft²/hr</span>
              </div>
            </div>
            
            <div className="bg-gray-50 p-3 rounded border border-gray-200">
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Storage Coefficient</h3>
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </div>
              <div className="flex justify-between items-center">
                <span className="text-lg font-bold">{formatAxisTick(analysisResult.storageCoefficient)}</span>
                <span className="text-sm text-gray-500">ft/psi</span>
              </div>
            </div>
            
            <div className="bg-gray-50 p-3 rounded border border-gray-200">
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Skin Factor</h3>
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </div>
              <div className="flex justify-between items-center">
                <span className="text-lg font-bold">{formatAxisTick(analysisResult.skinFactor)}</span>
                <span className="text-sm text-gray-500">dimensionless</span>
              </div>
            </div>
          </div>
          
          <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
            <h3 className="text-sm font-medium text-blue-800 mb-2">Interpretation</h3>
            <p className="text-sm text-blue-700">
              {selectedFlowRegime === 'impulse_linear' ? (
                <>
                  The data exhibits impulse linear flow behavior, indicating flow in a linear system.
                  This suggests a fracture-dominated flow regime with a calculated permeability of {formatAxisTick(analysisResult.permeability)} md.
                  The linear flow pattern indicates that the pressure transient is moving through a planar feature such as a fracture.
                </>
              ) : selectedFlowRegime === 'impulse_radial' ? (
                <>
                  The data exhibits impulse radial flow behavior, indicating flow in a radial system.
                  This suggests a matrix-dominated flow regime with a calculated permeability of {formatAxisTick(analysisResult.permeability)} md.
                  The radial flow pattern indicates that the pressure transient has moved beyond any near-wellbore effects and is propagating radially through the formation.
                </>
              ) : (
                <>
                  The flow regime is uncertain or complex. Consider reviewing the data and analysis parameters.
                </>
              )}
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default AfterClosureAnalysis;

