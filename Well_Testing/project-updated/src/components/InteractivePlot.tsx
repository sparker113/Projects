import React, { useState, useRef, useEffect } from 'react';
import { PlotData } from '../utils/plotUtils';

interface InteractivePlotProps {
  plotData: PlotData[];
  onAddSlope: (slope: { id: string; slope: number; x1: number; y1: number; x2: number; y2: number }) => void;
  onAddFlaggedPoint: (point: { id: string; x: number; y: number; comment: string }) => void;
  formatAxisTick: (value: number) => string;
  width: number;
  height: number;
}

const InteractivePlot: React.FC<InteractivePlotProps> = ({
  plotData,
  onAddSlope,
  onAddFlaggedPoint,
  formatAxisTick,
  width,
  height
}) => {
  const svgRef = useRef<SVGSVGElement>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [startPoint, setStartPoint] = useState<{ x: number; y: number } | null>(null);
  const [endPoint, setEndPoint] = useState<{ x: number; y: number } | null>(null);
  const [mode, setMode] = useState<'slope' | 'point'>('slope');
  const [showTooltip, setShowTooltip] = useState(false);
  const [tooltipPosition, setTooltipPosition] = useState({ x: 0, y: 0 });
  const [tooltipContent, setTooltipContent] = useState('');

  // Calculate plot bounds from data
  const getBounds = () => {
    if (!plotData || plotData.length === 0) {
      return { xMin: 0, xMax: 1, yMin: 0, yMax: 1 };
    }

    const xValues = plotData.map(d => d.time);
    const yValues = plotData.map(d => d.pressure).filter(p => p !== undefined && p !== null) as number[];
    
    const xMin = Math.min(...xValues);
    const xMax = Math.max(...xValues);
    const yMin = Math.min(...yValues);
    const yMax = Math.max(...yValues);
    
    // Add some padding
    const xPadding = (xMax - xMin) * 0.05;
    const yPadding = (yMax - yMin) * 0.05;
    
    return {
      xMin: xMin - xPadding,
      xMax: xMax + xPadding,
      yMin: yMin - yPadding,
      yMax: yMax + yPadding
    };
  };

  const bounds = getBounds();

  // Convert screen coordinates to data coordinates
  const screenToData = (screenX: number, screenY: number) => {
    if (!svgRef.current) return { x: 0, y: 0 };
    
    const svgRect = svgRef.current.getBoundingClientRect();
    const xRatio = (screenX - svgRect.left) / svgRect.width;
    const yRatio = (screenY - svgRect.top) / svgRect.height;
    
    const x = bounds.xMin + xRatio * (bounds.xMax - bounds.xMin);
    const y = bounds.yMax - yRatio * (bounds.yMax - bounds.yMin);
    
    return { x, y };
  };

  // Convert data coordinates to screen coordinates
  const dataToScreen = (dataX: number, dataY: number) => {
    if (!svgRef.current) return { x: 0, y: 0 };
    
    const svgRect = svgRef.current.getBoundingClientRect();
    
    const xRatio = (dataX - bounds.xMin) / (bounds.xMax - bounds.xMin);
    const yRatio = (bounds.yMax - dataY) / (bounds.yMax - bounds.yMin);
    
    const x = svgRect.left + xRatio * svgRect.width;
    const y = svgRect.top + yRatio * svgRect.height;
    
    return { x, y };
  };

  // Handle mouse down event
  const handleMouseDown = (e: React.MouseEvent<SVGSVGElement>) => {
    const point = screenToData(e.clientX, e.clientY);
    setStartPoint(point);
    setEndPoint(null);
    setIsDragging(true);
  };

  // Handle mouse move event
  const handleMouseMove = (e: React.MouseEvent<SVGSVGElement>) => {
    const point = screenToData(e.clientX, e.clientY);
    
    if (isDragging && startPoint) {
      setEndPoint(point);
      
      if (mode === 'slope') {
        // Calculate slope
        const dx = point.x - startPoint.x;
        const dy = point.y - startPoint.y;
        const slope = dx !== 0 ? dy / dx : Infinity;
        
        // Update tooltip
        setTooltipContent(`Slope: ${formatAxisTick(slope)}`);
        setTooltipPosition({ x: e.clientX, y: e.clientY });
        setShowTooltip(true);
      }
    } else {
      // Show coordinates in tooltip
      setTooltipContent(`(${formatAxisTick(point.x)}, ${formatAxisTick(point.y)})`);
      setTooltipPosition({ x: e.clientX, y: e.clientY });
      setShowTooltip(true);
    }
  };

  // Handle mouse up event
  const handleMouseUp = () => {
    if (isDragging && startPoint && endPoint) {
      if (mode === 'slope') {
        // Calculate slope
        const dx = endPoint.x - startPoint.x;
        const dy = endPoint.y - startPoint.y;
        const slope = dx !== 0 ? dy / dx : Infinity;
        
        // Add slope
        onAddSlope({
          id: `slope-${Date.now()}`,
          slope,
          x1: startPoint.x,
          y1: startPoint.y,
          x2: endPoint.x,
          y2: endPoint.y
        });
      } else {
        // Add flagged point
        onAddFlaggedPoint({
          id: `point-${Date.now()}`,
          x: endPoint.x,
          y: endPoint.y,
          comment: 'New point'
        });
      }
    }
    
    setIsDragging(false);
    setStartPoint(null);
    setEndPoint(null);
    setShowTooltip(false);
  };

  // Handle mouse leave event
  const handleMouseLeave = () => {
    setShowTooltip(false);
    if (isDragging) {
      setIsDragging(false);
      setStartPoint(null);
      setEndPoint(null);
    }
  };

  return (
    <div className="relative">
      <div className="mb-2 flex gap-2">
        <button
          type="button"
          className={`px-3 py-1 text-sm font-medium rounded-lg ${
            mode === 'slope'
              ? 'bg-primary-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
          onClick={() => setMode('slope')}
        >
          Draw Slope
        </button>
        <button
          type="button"
          className={`px-3 py-1 text-sm font-medium rounded-lg ${
            mode === 'point'
              ? 'bg-primary-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
          onClick={() => setMode('point')}
        >
          Flag Point
        </button>
      </div>
      
      <svg
        ref={svgRef}
        width={width}
        height={height}
        className="absolute top-0 left-0 z-10 cursor-crosshair"
        style={{ pointerEvents: 'all', opacity: 0 }}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseLeave}
      >
        <rect x={0} y={0} width={width} height={height} fill="transparent" />
        
        {isDragging && startPoint && endPoint && mode === 'slope' && (
          <line
            x1={dataToScreen(startPoint.x, startPoint.y).x}
            y1={dataToScreen(startPoint.x, startPoint.y).y}
            x2={dataToScreen(endPoint.x, endPoint.y).x}
            y2={dataToScreen(endPoint.x, endPoint.y).y}
            stroke="red"
            strokeWidth={2}
            strokeDasharray="5,5"
          />
        )}
        
        {isDragging && startPoint && endPoint && mode === 'point' && (
          <circle
            cx={dataToScreen(endPoint.x, endPoint.y).x}
            cy={dataToScreen(endPoint.x, endPoint.y).y}
            r={5}
            fill="orange"
          />
        )}
      </svg>
      
      {showTooltip && (
        <div
          className="absolute z-20 bg-white px-2 py-1 text-xs border border-gray-300 rounded shadow-md"
          style={{
            left: tooltipPosition.x + 10,
            top: tooltipPosition.y - 20,
            pointerEvents: 'none'
          }}
        >
          {tooltipContent}
        </div>
      )}
    </div>
  );
};

export default InteractivePlot;

