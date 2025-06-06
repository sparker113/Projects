import React, { useState, useEffect } from 'react';
import { TestData } from '../types/wellTest';
import { 
  ClosureAnalysisMethod, 
  ClosurePoint, 
  ClosureAnalysisResult,
  detectClosure,
  performClosureAnalysis
} from '../utils/closureAnalysis';
import { createPlotData, PlotData } from '../utils/plotUtils';
import { TimeFunction, getTimeFunctionLabel } from '../utils/timeFunctions';
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
import { HelpCircle, ChevronDown, ChevronUp } from 'lucide-react';

interface ClosureAnalysisProps {
  testData: TestData;
}

const ClosureAnalysis: React.FC<ClosureAnalysisProps> = ({ testData }) => {
  // State for analysis configuration
  const [analysisMethod, setAnalysisMethod] = useState<ClosureAnalysisMethod>('g-function');
  const [timeFunction, setTimeFunction] = useState<TimeFunction>('elapsed_time');
  const [showAdvancedOptions, setShowAdvancedOptions] = useState(false);
  
  // State for analysis results
  const [closurePoint, setClosurePoint] = useState<ClosurePoint | null>(null);
  const [analysisResult, setAnalysisResult] = useState<ClosureAnalysisResult | null>(null);
  const [plotData, setPlotData] = useState<PlotData[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  
  // State for manual closure selection
  const [manualClosureIndex, setManualClosureIndex] = useState<number | null>(null);
  
  // Run closure analysis when method changes
  useEffect(() => {
    if (testData) {
      setIsLoading(true);
      
      try {
        // Detect closure using selected method
        const closure = detectClosure(testData, analysisMethod);
        setClosurePoint(closure);
        
        // Perform comprehensive analysis
        const result = performClosureAnalysis(testData);
        setAnalysisResult(result);
        
        // Generate plot data based on analysis method
        let data: PlotData[];
        switch (analysisMethod) {
          case 'g-function':
            data = createGFunctionPlotData(testData);
            break;
          case 'square-root-time':
            data = createSqrtTimePlotData(testData);
            break;
          case 'log-log':
            data = createLogLogPlotData(testData);
            break;
          case 'derivative':
            data = createDerivativePlotData(testData);
            break;
          case 'tangent':
            data = createTangentPlotData(testData);
            break;
          default:
            data = createGFunctionPlotData(testData);
        }
        
        setPlotData(data);
      } catch (error) {
        console.error('Error in closure analysis:', error);
      }
      
      setIsLoading(false);
    }
  }, [testData, analysisMethod]);
  
  // Create G-function plot data
  const createGFunctionPlotData = (data: TestData): PlotData[] => {
    const { time, pressure } = data;
    const g0 = 0.469; // G-function constant
    
    // Assume the first point is the shut-in time for DFIT
    const shutInTime = time[0];
    const shutInPressure = pressure[0];
    
    return time.map((t, i) => {
      const deltaT = t - shutInTime;
      if (deltaT <= 0) {
        return {
          name: t.toString(),
          time: 0,
          pressure: 0,
          derivative: 0,
          rawPressure: pressure[i]
        };
      }
      
      // Calculate g-function
      const g = g0 * (4 * deltaT / shutInTime * (1 + deltaT / shutInTime));
      
      // Calculate derivative if not at endpoints
      let derivative = 0;
      if (i > 0 && i < time.length - 1) {
        const dp = pressure[i + 1] - pressure[i - 1];
        const dg = g * 2; // Approximate derivative window
        derivative = dp / dg;
      }
      
      return {
        name: t.toString(),
        time: g,
        pressure: pressure[i] - shutInPressure,
        derivative,
        rawPressure: pressure[i]
      };
    });
  };
  
  // Create square root of time plot data
  const createSqrtTimePlotData = (data: TestData): PlotData[] => {
    const { time, pressure } = data;
    
    // Calculate pressure derivative with respect to sqrt(time)
    const derivatives: number[] = [];
    const sqrtTimes = time.map(t => Math.sqrt(Math.max(0, t)));
    
    for (let i = 1; i < time.length - 1; i++) {
      const dP = pressure[i + 1] - pressure[i - 1];
      const dSqrtT = sqrtTimes[i + 1] - sqrtTimes[i - 1];
      derivatives.push(dP / dSqrtT);
    }
    
    // Add endpoints
    derivatives.unshift(derivatives[0] || 0);
    derivatives.push(derivatives[derivatives.length - 1] || 0);
    
    return time.map((t, i) => ({
      name: t.toString(),
      time: sqrtTimes[i],
      pressure: pressure[i],
      derivative: derivatives[i],
      rawPressure: pressure[i]
    }));
  };
  
  // Create log-log plot data
  const createLogLogPlotData = (data: TestData): PlotData[] => {
    const { time, pressure } = data;
    
    // Calculate log-log values
    const logTime = time.map(t => Math.log10(Math.max(t, 1e-10)));
    const logPressure = pressure.map(p => Math.log10(Math.max(p, 1e-10)));
    
    // Calculate derivatives
    const derivatives: number[] = [];
    for (let i = 1; i < logTime.length - 1; i++) {
      const dLogP = logPressure[i + 1] - logPressure[i - 1];
      const dLogT = logTime[i + 1] - logTime[i - 1];
      derivatives.push(dLogP / dLogT);
    }
    
    // Add endpoints
    derivatives.unshift(derivatives[0] || 0);
    derivatives.push(derivatives[derivatives.length - 1] || 0);
    
    return time.map((t, i) => ({
      name: t.toString(),
      time: logTime[i],
      pressure: logPressure[i],
      derivative: derivatives[i],
      rawPressure: pressure[i]
    }));
  };
  
  // Create derivative plot data
  const createDerivativePlotData = (data: TestData): PlotData[] => {
    const { time, pressure } = data;
    
    // Calculate pressure derivative
    const derivatives: number[] = [];
    for (let i = 1; i < time.length - 1; i++) {
      const dP = pressure[i + 1] - pressure[i - 1];
      const dT = time[i + 1] - time[i - 1];
      derivatives.push(dP / dT);
    }
    
    // Add endpoints
    derivatives.unshift(derivatives[0] || 0);
    derivatives.push(derivatives[derivatives.length - 1] || 0);
    
    return time.map((t, i) => ({
      name: t.toString(),
      time: t,
      pressure: pressure[i],
      derivative: derivatives[i],
      rawPressure: pressure[i]
    }));
  };
  
  // Create tangent method plot data
  const createTangentPlotData = (data: TestData): PlotData[] => {
    const { time, pressure } = data;
    
    // Find maximum pressure
    const maxPressureIndex = pressure.indexOf(Math.max(...pressure));
    
    // Calculate early and late time regions
    const earlyTimeRegion = {
      time: time.slice(0, Math.floor(time.length / 3)),
      pressure: pressure.slice(0, Math.floor(pressure.length / 3))
    };
    
    const lateTimeRegion = {
      time: time.slice(Math.floor(2 * time.length / 3)),
      pressure: pressure.slice(Math.floor(2 * pressure.length / 3))
    };
    
    // Calculate slopes
    const earlyTimeSlope = calculateSlope(earlyTimeRegion.time, earlyTimeRegion.pressure);
    const lateTimeSlope = calculateSlope(lateTimeRegion.time, lateTimeRegion.pressure);
    
    // Calculate tangent lines
    const earlyTangent: number[] = [];
    const lateTangent: number[] = [];
    
    for (let i = 0; i < time.length; i++) {
      earlyTangent.push(earlyTimeSlope.slope * time[i] + earlyTimeSlope.intercept);
      lateTangent.push(lateTimeSlope.slope * time[i] + lateTimeSlope.intercept);
    }
    
    return time.map((t, i) => ({
      name: t.toString(),
      time: t,
      pressure: pressure[i],
      earlyTangent: earlyTangent[i],
      lateTangent: lateTangent[i],
      rawPressure: pressure[i]
    }));
  };
  
  // Calculate slope and intercept
  const calculateSlope = (x: number[], y: number[]): { slope: number; intercept: number } => {
    const n = x.length;
    const sumX = x.reduce((a, b) => a + b, 0);
    const sumY = y.reduce((a, b) => a + b, 0);
    const sumXY = x.reduce((sum, xi, i) => sum + xi * y[i], 0);
    const sumXX = x.reduce((sum, xi) => sum + xi * xi, 0);
    
    const slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
    const intercept = (sumY - slope * sumX) / n;
    
    return { slope, intercept };
  };
  
  // Format axis tick values
  const formatAxisTick = (value: number) => {
    if (Math.abs(value) < 0.01 || Math.abs(value) > 9999) {
      return value.toExponential(2);
    }
    return value.toFixed(2);
  };
  
  // Handle manual closure selection
  const handleManualClosureSelection = (point: any) => {
    if (!point || !testData) return;
    
    // Find the closest point in the data
    const { time, pressure } = testData;
    let closestIndex = 0;
    let minDistance = Infinity;
    
    for (let i = 0; i < time.length; i++) {
      const plotX = plotData[i]?.time || 0;
      const distance = Math.abs(plotX - point.time);
      
      if (distance < minDistance) {
        minDistance = distance;
        closestIndex = i;
      }
    }
    
    // Update manual closure point
    setManualClosureIndex(closestIndex);
    
    // Create a new closure point
    const manualClosure: ClosurePoint = {
      index: closestIndex,
      time: time[closestIndex],
      pressure: pressure[closestIndex],
      method: analysisMethod,
      confidence: 1 // Manual selection has 100% confidence
    };
    
    setClosurePoint(manualClosure);
  };
  
  // Get axis labels based on analysis method
  const getAxisLabel = () => {
    switch (analysisMethod) {
      case 'g-function':
        return {
          x: 'G-Function',
          y: `Pressure (${testData.pressureUnit})`
        };
      case 'square-root-time':
        return {
          x: 'Square Root of Time',
          y: `Pressure (${testData.pressureUnit})`
        };
      case 'log-log':
        return {
          x: 'Log Time',
          y: 'Log Pressure'
        };
      case 'derivative':
      case 'tangent':
        return {
          x: 'Time',
          y: `Pressure (${testData.pressureUnit})`
        };
      default:
        return {
          x: 'Time',
          y: 'Pressure'
        };
    }
  };
  
  return (
    <div className="space-y-6">
      <div className="bg-white shadow-md rounded-lg p-4">
        <h2 className="text-xl font-semibold mb-4">Closure Analysis</h2>
        
        {/* Analysis method selection */}
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Analysis Method
          </label>
          <div className="flex flex-wrap gap-2">
            <button
              className={`px-3 py-1.5 text-sm font-medium rounded-lg ${
                analysisMethod === 'g-function'
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
              onClick={() => setAnalysisMethod('g-function')}
            >
              G-Function
            </button>
            <button
              className={`px-3 py-1.5 text-sm font-medium rounded-lg ${
                analysisMethod === 'square-root-time'
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
              onClick={() => setAnalysisMethod('square-root-time')}
            >
              Square Root Time
            </button>
            <button
              className={`px-3 py-1.5 text-sm font-medium rounded-lg ${
                analysisMethod === 'log-log'
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
              onClick={() => setAnalysisMethod('log-log')}
            >
              Log-Log
            </button>
            <button
              className={`px-3 py-1.5 text-sm font-medium rounded-lg ${
                analysisMethod === 'derivative'
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
              onClick={() => setAnalysisMethod('derivative')}
            >
              Derivative
            </button>
            <button
              className={`px-3 py-1.5 text-sm font-medium rounded-lg ${
                analysisMethod === 'tangent'
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
              onClick={() => setAnalysisMethod('tangent')}
            >
              Tangent
            </button>
          </div>
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
                    Time Function
                  </label>
                  <select
                    value={timeFunction}
                    onChange={(e) => setTimeFunction(e.target.value as TimeFunction)}
                    className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    disabled={['g-function', 'square-root-time', 'log-log'].includes(analysisMethod)}
                  >
                    <option value="elapsed_time">Elapsed Time</option>
                    <option value="log_time">Log Time</option>
                    <option value="sqrt_time">Square Root of Time</option>
                    <option value="squared_time">Squared Time</option>
                    <option value="reciprocal_time">Reciprocal Time</option>
                  </select>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Manual Closure Selection
                  </label>
                  <p className="text-sm text-gray-600">
                    Click on the plot to manually select closure point
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>
        
        {/* Plot area */}
        <div className="bg-gray-50 rounded-lg border border-gray-200 p-4 h-[400px] relative">
          {isLoading ? (
            <div className="flex items-center justify-center h-full">
              <div className="text-center">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent mb-2"></div>
                <p className="text-gray-700">Analyzing closure...</p>
              </div>
            </div>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <LineChart
                data={plotData}
                margin={{ top: 20, right: 30, left: 20, bottom: 20 }}
                onClick={handleManualClosureSelection}
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
                
                {/* Pressure line */}
                <Line
                  type="monotone"
                  dataKey="pressure"
                  stroke="#4338ca"
                  name="Pressure"
                  dot={false}
                  strokeWidth={2}
                />
                
                {/* Derivative line */}
                {analysisMethod !== 'tangent' && (
                  <Line
                    type="monotone"
                    dataKey="derivative"
                    stroke="#16a34a"
                    name="Derivative"
                    dot={false}
                    strokeWidth={2}
                  />
                )}
                
                {/* Tangent lines for tangent method */}
                {analysisMethod === 'tangent' && (
                  <>
                    <Line
                      type="monotone"
                      dataKey="earlyTangent"
                      stroke="#ef4444"
                      name="Early Tangent"
                      dot={false}
                      strokeWidth={2}
                      strokeDasharray="5 5"
                    />
                    <Line
                      type="monotone"
                      dataKey="lateTangent"
                      stroke="#f59e0b"
                      name="Late Tangent"
                      dot={false}
                      strokeWidth={2}
                      strokeDasharray="5 5"
                    />
                  </>
                )}
                
                {/* Closure point */}
                {closurePoint && (
                  <ReferenceLine
                    x={plotData[closurePoint.index]?.time}
                    stroke="#ef4444"
                    strokeWidth={2}
                    label={{
                      value: 'Closure',
                      position: 'top',
                      fill: '#ef4444',
                      fontSize: 12
                    }}
                  />
                )}
                
                {/* ISIP point */}
                {analysisResult && (
                  <ReferenceLine
                    y={analysisResult.isip}
                    stroke="#0ea5e9"
                    strokeWidth={2}
                    strokeDasharray="5 5"
                    label={{
                      value: 'ISIP',
                      position: 'right',
                      fill: '#0ea5e9',
                      fontSize: 12
                    }}
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
          <h2 className="text-xl font-semibold mb-4">Closure Analysis Results</h2>
          
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            <div className="bg-gray-50 p-3 rounded border border-gray-200">
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Closure Pressure</h3>
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </div>
              <div className="flex justify-between items-center">
                <span className="text-lg font-bold">{formatAxisTick(closurePoint?.pressure || 0)}</span>
                <span className="text-sm text-gray-500">{testData.pressureUnit}</span>
              </div>
              <div className="text-xs text-gray-500 mt-1">
                Method: {closurePoint?.method || 'Unknown'}
              </div>
            </div>
            
            <div className="bg-gray-50 p-3 rounded border border-gray-200">
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">ISIP</h3>
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </div>
              <div className="flex justify-between items-center">
                <span className="text-lg font-bold">{formatAxisTick(analysisResult.isip)}</span>
                <span className="text-sm text-gray-500">{testData.pressureUnit}</span>
              </div>
            </div>
            
            <div className="bg-gray-50 p-3 rounded border border-gray-200">
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Minimum Stress</h3>
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </div>
              <div className="flex justify-between items-center">
                <span className="text-lg font-bold">{formatAxisTick(analysisResult.minStress)}</span>
                <span className="text-sm text-gray-500">{testData.pressureUnit}</span>
              </div>
            </div>
            
            <div className="bg-gray-50 p-3 rounded border border-gray-200">
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Process Zone Stress</h3>
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </div>
              <div className="flex justify-between items-center">
                <span className="text-lg font-bold">{formatAxisTick(analysisResult.processZoneStress)}</span>
                <span className="text-sm text-gray-500">{testData.pressureUnit}</span>
              </div>
            </div>
            
            <div className="bg-gray-50 p-3 rounded border border-gray-200">
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Leak-Off Coefficient</h3>
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </div>
              <div className="flex justify-between items-center">
                <span className="text-lg font-bold">{formatAxisTick(analysisResult.leakOffCoefficient)}</span>
                <span className="text-sm text-gray-500">ft/√min</span>
              </div>
            </div>
            
            <div className="bg-gray-50 p-3 rounded border border-gray-200">
              <div className="flex items-center mb-1">
                <h3 className="text-sm font-medium text-gray-700">Fracture Pressure</h3>
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </div>
              <div className="flex justify-between items-center">
                <span className="text-lg font-bold">{formatAxisTick(analysisResult.fracturePressure)}</span>
                <span className="text-sm text-gray-500">{testData.pressureUnit}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ClosureAnalysis;

