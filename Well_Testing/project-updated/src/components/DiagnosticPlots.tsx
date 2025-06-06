import React, { useState, useEffect, useRef } from 'react';
import { TestData, FlowRegime, AnalysisType } from '../types/wellTest';
import { 
  createPlotData, 
  PlotType,
  PlotData
} from '../utils/plotUtils';
import { TimeFunction, getTimeFunctionLabel } from '../utils/timeFunctions';
import InteractivePlot from './InteractivePlot';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ReferenceLine
} from 'recharts';
import { ChevronDown, ChevronUp, Settings } from 'lucide-react';

interface DiagnosticPlotsProps {
  testData: TestData;
  identifiedFlowRegime: FlowRegime | null;
  analysisType: AnalysisType;
}

const DiagnosticPlots: React.FC<DiagnosticPlotsProps> = ({ 
  testData, 
  identifiedFlowRegime,
  analysisType
}) => {
  // State for plot configuration
  const [activePlot, setActivePlot] = useState<PlotType>('log-log');
  const [timeFunction, setTimeFunction] = useState<TimeFunction>('elapsed_time');
  const [showPressure, setShowPressure] = useState<boolean>(true);
  const [showDerivative, setShowDerivative] = useState<boolean>(true);
  const [showPrimaryDerivative, setShowPrimaryDerivative] = useState<boolean>(false);
  const [showRawPressure, setShowRawPressure] = useState<boolean>(false);
  const [showAdvancedOptions, setShowAdvancedOptions] = useState<boolean>(false);
  
  // State for plot data
  const [plotData, setPlotData] = useState<PlotData[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  
  // State for interactive elements
  const [slopes, setSlopes] = useState<{id: string, slope: number, x1: number, y1: number, x2: number, y2: number}[]>([]);
  const [flaggedPoints, setFlaggedPoints] = useState<{id: string, x: number, y: number, comment: string}[]>([]);
  
  // Generate plot data when plot type or time function changes
  useEffect(() => {
    if (testData) {
      setIsLoading(true);
      
      const data = createPlotData(testData, activePlot, timeFunction);
      setPlotData(data);
      
      setIsLoading(false);
    }
  }, [testData, activePlot, timeFunction]);

  // Get axis labels based on plot type and time function
  const getAxisLabel = () => {
    switch (activePlot) {
      case 'log-log':
        return {
          x: 'Log Time',
          y: 'Log Pressure/Derivative'
        };
      case 'horner':
        return {
          x: 'Horner Time',
          y: `Pressure (${testData.pressureUnit})`
        };
      case 'mdh':
        return {
          x: 'Log Time',
          y: `Pressure (${testData.pressureUnit})`
        };
      case 'g-function':
        return {
          x: 'G-Function',
          y: `Pressure (${testData.pressureUnit})`
        };
      case 'pressure':
      case 'pressure-derivative':
      case 'custom':
        return {
          x: getTimeFunctionLabel(timeFunction),
          y: `Pressure/Derivative (${testData.pressureUnit})`
        };
      default:
        return {
          x: 'Time',
          y: 'Pressure'
        };
    }
  };
  
  // Get flow regime description
  const getFlowRegimeDescription = (regime: FlowRegime | null): string => {
    if (!regime) return 'No flow regime identified';
    
    switch (regime) {
      case 'radial_flow':
        return 'Radial flow regime identified: Horizontal line on pressure derivative plot';
      case 'linear_flow':
        return 'Linear flow regime identified: 1/2 slope on pressure derivative plot';
      case 'spherical_flow':
        return 'Spherical flow regime identified: -1/2 slope on pressure derivative plot';
      case 'boundary_dominated':
        return 'Boundary dominated flow: Unit slope at late times';
      case 'wellbore_storage':
        return 'Wellbore storage effects: Unit slope at early times';
      case 'dual_porosity':
        return 'Dual porosity behavior: Depression in pressure derivative';
      default:
        return 'Flow regime uncertain or complex';
    }
  };

  // Format axis tick values
  const formatAxisTick = (value: number) => {
    if (Math.abs(value) < 0.01 || Math.abs(value) > 9999) {
      return value.toExponential(2);
    }
    return value.toFixed(2);
  };
  
  // Handle adding a slope line
  const handleAddSlope = () => {
    // Add a default slope line
    const newSlope = {
      id: `slope-${Date.now()}`,
      slope: 1, // Default unit slope
      x1: plotData[0]?.time || 0,
      y1: plotData[0]?.pressure || 0,
      x2: plotData[plotData.length - 1]?.time || 1,
      y2: plotData[plotData.length - 1]?.pressure || 1
    };
    setSlopes([...slopes, newSlope]);
  };
  
  // Handle adding a flagged point
  const handleAddFlaggedPoint = () => {
    // Add a default flagged point at the middle of the data
    const midIndex = Math.floor(plotData.length / 2);
    const newPoint = {
      id: `point-${Date.now()}`,
      x: plotData[midIndex]?.time || 0,
      y: plotData[midIndex]?.pressure || 0,
      comment: 'New point'
    };
    setFlaggedPoints([...flaggedPoints, newPoint]);
  };
  
  return (
    <div>
      {/* Plot type selection tabs */}
      <div className="flex mb-4 border-b border-gray-200 overflow-x-auto">
        {analysisType === 'PBU' ? (
          <>
            <button
              className={`px-4 py-2 whitespace-nowrap ${activePlot === 'log-log' ? 'border-b-2 border-primary-600 text-primary-700 font-medium' : 'text-gray-700 hover:text-gray-900'}`}
              onClick={() => setActivePlot('log-log')}
            >
              Log-Log Diagnostic
            </button>
            <button
              className={`px-4 py-2 whitespace-nowrap ${activePlot === 'horner' ? 'border-b-2 border-primary-600 text-primary-700 font-medium' : 'text-gray-700 hover:text-gray-900'}`}
              onClick={() => setActivePlot('horner')}
            >
              Horner Plot
            </button>
            <button
              className={`px-4 py-2 whitespace-nowrap ${activePlot === 'mdh' ? 'border-b-2 border-primary-600 text-primary-700 font-medium' : 'text-gray-700 hover:text-gray-900'}`}
              onClick={() => setActivePlot('mdh')}
            >
              MDH Plot
            </button>
            <button
              className={`px-4 py-2 whitespace-nowrap ${activePlot === 'pressure' ? 'border-b-2 border-primary-600 text-primary-700 font-medium' : 'text-gray-700 hover:text-gray-900'}`}
              onClick={() => setActivePlot('pressure')}
            >
              Pressure
            </button>
            <button
              className={`px-4 py-2 whitespace-nowrap ${activePlot === 'pressure-derivative' ? 'border-b-2 border-primary-600 text-primary-700 font-medium' : 'text-gray-700 hover:text-gray-900'}`}
              onClick={() => setActivePlot('pressure-derivative')}
            >
              Pressure Derivative
            </button>
            <button
              className={`px-4 py-2 whitespace-nowrap ${activePlot === 'custom' ? 'border-b-2 border-primary-600 text-primary-700 font-medium' : 'text-gray-700 hover:text-gray-900'}`}
              onClick={() => setActivePlot('custom')}
            >
              Custom Plot
            </button>
          </>
        ) : (
          <>
            <button
              className={`px-4 py-2 whitespace-nowrap ${activePlot === 'g-function' ? 'border-b-2 border-primary-600 text-primary-700 font-medium' : 'text-gray-700 hover:text-gray-900'}`}
              onClick={() => setActivePlot('g-function')}
            >
              G-Function Plot
            </button>
            <button
              className={`px-4 py-2 whitespace-nowrap ${activePlot === 'log-log' ? 'border-b-2 border-primary-600 text-primary-700 font-medium' : 'text-gray-700 hover:text-gray-900'}`}
              onClick={() => setActivePlot('log-log')}
            >
              Log-Log Plot
            </button>
            <button
              className={`px-4 py-2 whitespace-nowrap ${activePlot === 'pressure' ? 'border-b-2 border-primary-600 text-primary-700 font-medium' : 'text-gray-700 hover:text-gray-900'}`}
              onClick={() => setActivePlot('pressure')}
            >
              Pressure
            </button>
            <button
              className={`px-4 py-2 whitespace-nowrap ${activePlot === 'pressure-derivative' ? 'border-b-2 border-primary-600 text-primary-700 font-medium' : 'text-gray-700 hover:text-gray-900'}`}
              onClick={() => setActivePlot('pressure-derivative')}
            >
              Pressure Derivative
            </button>
            <button
              className={`px-4 py-2 whitespace-nowrap ${activePlot === 'custom' ? 'border-b-2 border-primary-600 text-primary-700 font-medium' : 'text-gray-700 hover:text-gray-900'}`}
              onClick={() => setActivePlot('custom')}
            >
              Custom Plot
            </button>
          </>
        )}
      </div>
      
      {/* Plot configuration options */}
      <div className="mb-4">
        <button
          type="button"
          onClick={() => setShowAdvancedOptions(!showAdvancedOptions)}
          className="flex items-center text-sm text-gray-600 hover:text-gray-900 mb-2"
        >
          {showAdvancedOptions ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
          <Settings size={16} className="mx-1" />
          <span className="ml-1">Plot Options</span>
        </button>
        
        {showAdvancedOptions && (
          <div className="bg-gray-50 p-4 rounded-lg border border-gray-200 mb-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Time function selection */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Time Function
                </label>
                <select
                  value={timeFunction}
                  onChange={(e) => setTimeFunction(e.target.value as TimeFunction)}
                  className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                  disabled={['log-log', 'horner', 'mdh', 'g-function'].includes(activePlot)}
                >
                  <option value="elapsed_time">Elapsed Time</option>
                  <option value="log_time">Log Time</option>
                  <option value="sqrt_time">Square Root of Time</option>
                  <option value="squared_time">Squared Time</option>
                  <option value="reciprocal_time">Reciprocal Time</option>
                  <option value="superposition_time">Superposition Time</option>
                  <option value="horner_time">Horner Time</option>
                  <option value="agarwal_time">Agarwal Equivalent Time</option>
                </select>
              </div>
              
              {/* Series visibility toggles */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Series Visibility
                </label>
                <div className="flex flex-wrap gap-3">
                  <label className="inline-flex items-center">
                    <input
                      type="checkbox"
                      checked={showPressure}
                      onChange={(e) => setShowPressure(e.target.checked)}
                      className="rounded border-gray-300 text-primary-600 shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    />
                    <span className="ml-2 text-sm text-gray-700">Pressure</span>
                  </label>
                  
                  <label className="inline-flex items-center">
                    <input
                      type="checkbox"
                      checked={showDerivative}
                      onChange={(e) => setShowDerivative(e.target.checked)}
                      className="rounded border-gray-300 text-primary-600 shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    />
                    <span className="ml-2 text-sm text-gray-700">Derivative</span>
                  </label>
                  
                  <label className="inline-flex items-center">
                    <input
                      type="checkbox"
                      checked={showPrimaryDerivative}
                      onChange={(e) => setShowPrimaryDerivative(e.target.checked)}
                      className="rounded border-gray-300 text-primary-600 shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    />
                    <span className="ml-2 text-sm text-gray-700">Primary Derivative (dP)</span>
                  </label>
                  
                  <label className="inline-flex items-center">
                    <input
                      type="checkbox"
                      checked={showRawPressure}
                      onChange={(e) => setShowRawPressure(e.target.checked)}
                      className="rounded border-gray-300 text-primary-600 shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    />
                    <span className="ml-2 text-sm text-gray-700">Raw Pressure</span>
                  </label>
                </div>
              </div>
            </div>
            
            {/* Interactive elements controls */}
            <div className="mt-4 flex gap-2">
              <button
                type="button"
                onClick={handleAddSlope}
                className="px-3 py-1 text-sm font-medium text-primary-700 bg-primary-50 border border-primary-200 rounded-lg hover:bg-primary-100"
              >
                Add Slope Line
              </button>
              
              <button
                type="button"
                onClick={handleAddFlaggedPoint}
                className="px-3 py-1 text-sm font-medium text-primary-700 bg-primary-50 border border-primary-200 rounded-lg hover:bg-primary-100"
              >
                Flag Point
              </button>
            </div>
            
            {/* Slope lines list */}
            {slopes.length > 0 && (
              <div className="mt-3">
                <h4 className="text-sm font-medium text-gray-700 mb-1">Slope Lines</h4>
                <div className="max-h-32 overflow-y-auto">
                  {slopes.map(slope => (
                    <div key={slope.id} className="flex items-center justify-between py-1 border-b border-gray-100">
                      <span className="text-xs text-gray-600">Slope: {slope.slope.toFixed(2)}</span>
                      <button
                        type="button"
                        onClick={() => setSlopes(slopes.filter(s => s.id !== slope.id))}
                        className="text-xs text-red-600 hover:text-red-800"
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}
            
            {/* Flagged points list */}
            {flaggedPoints.length > 0 && (
              <div className="mt-3">
                <h4 className="text-sm font-medium text-gray-700 mb-1">Flagged Points</h4>
                <div className="max-h-32 overflow-y-auto">
                  {flaggedPoints.map(point => (
                    <div key={point.id} className="flex items-center justify-between py-1 border-b border-gray-100">
                      <div>
                        <span className="text-xs text-gray-600 mr-2">
                          ({formatAxisTick(point.x)}, {formatAxisTick(point.y)})
                        </span>
                        <input
                          type="text"
                          value={point.comment}
                          onChange={(e) => {
                            const updatedPoints = flaggedPoints.map(p => 
                              p.id === point.id ? {...p, comment: e.target.value} : p
                            );
                            setFlaggedPoints(updatedPoints);
                          }}
                          className="text-xs border-gray-300 rounded-md p-1 w-32"
                          placeholder="Comment"
                        />
                      </div>
                      <button
                        type="button"
                        onClick={() => setFlaggedPoints(flaggedPoints.filter(p => p.id !== point.id))}
                        className="text-xs text-red-600 hover:text-red-800"
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </div>
      
      {/* Flow regime indicator */}
      <div className="relative min-h-[400px] bg-gray-50 rounded-lg border border-gray-200 flex items-center justify-center">
        <div className="w-full h-full p-4">
          <div className="border border-gray-300 bg-white rounded-lg p-3 mb-3 shadow-soft">
            <div className="flex items-center">
              <div className={`w-3 h-3 rounded-full ${identifiedFlowRegime ? 'bg-green-600' : 'bg-yellow-600'} mr-2`}></div>
              <p className="text-sm text-gray-800">
                {getFlowRegimeDescription(identifiedFlowRegime)}
              </p>
            </div>
          </div>
          
          {/* Plot area */}
          <div className="bg-white rounded-lg shadow-soft p-4 h-[400px] flex items-center justify-center relative">
            {isLoading ? (
              <div className="text-center">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent mb-2"></div>
                <p className="text-gray-700">Generating plot...</p>
              </div>
            ) : (
              <>
                <ResponsiveContainer width="100%" height={400}>
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
                    
                    {/* Pressure line */}
                    {showPressure && (
                      <Line
                        type="monotone"
                        dataKey="pressure"
                        stroke="#4338ca"
                        name="Pressure"
                        dot={false}
                        strokeWidth={2}
                      />
                    )}
                    
                    {/* Derivative line */}
                    {showDerivative && (
                      <Line
                        type="monotone"
                        dataKey="derivative"
                        stroke="#16a34a"
                        name="Derivative"
                        dot={false}
                        strokeWidth={2}
                      />
                    )}
                    
                    {/* Primary derivative line */}
                    {showPrimaryDerivative && (
                      <Line
                        type="monotone"
                        dataKey="primaryDerivative"
                        stroke="#ea580c"
                      name="Primary Derivative (dP)"
                      dot={false}
                      strokeWidth={2}
                    />
                  )}
                  
                  {/* Raw pressure line */}
                  {showRawPressure && (
                    <Line
                      type="monotone"
                      dataKey="rawPressure"
                      stroke="#9333ea"
                      name="Raw Pressure"
                      dot={false}
                      strokeWidth={2}
                    />
                  )}
                  
                  {/* Slope reference lines */}
                  {slopes.map(slope => (
                    <ReferenceLine
                      key={slope.id}
                      segment={[
                        { x: slope.x1, y: slope.y1 },
                        { x: slope.x2, y: slope.y2 }
                      ]}
                      stroke="#ef4444"
                      strokeWidth={2}
                      strokeDasharray="5 5"
                      label={{ value: `m=${slope.slope.toFixed(2)}`, position: 'insideBottomRight' }}
                    />
                  ))}
                  
                  {/* Flagged points */}
                  {flaggedPoints.map(point => (
                    <ReferenceLine
                      key={point.id}
                      x={point.x}
                      stroke="#f59e0b"
                      strokeWidth={2}
                      label={{ 
                        value: point.comment, 
                        position: 'top',
                        fill: '#f59e0b',
                        fontSize: 12
                      }}
                    />
                  ))}
                </LineChart>
                </ResponsiveContainer>
                
                {/* Interactive plot overlay */}
                <InteractivePlot
                  plotData={plotData}
                  onAddSlope={(slope) => setSlopes([...slopes, slope])}
                  onAddFlaggedPoint={(point) => setFlaggedPoints([...flaggedPoints, point])}
                  formatAxisTick={formatAxisTick}
                  width={800}
                  height={400}
                />
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default DiagnosticPlots;

