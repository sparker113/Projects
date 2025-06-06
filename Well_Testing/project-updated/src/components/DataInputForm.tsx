import React, { useState } from 'react';
import { HelpCircle, Upload, ChevronDown, ChevronUp, RefreshCw } from 'lucide-react';
import { TestData, FluidProperties, WellProperties, PressureUnit, TimeUnit, FlowRateUnit, LengthUnit, ViscosityUnit, AnalysisType, WellOrientation, BoundaryType, BHPCorrectionMethod, DensityUnit, CompressibilityUnit, StorageUnit, DefaultFlags } from '../types/wellTest';
import Tooltip from './Tooltip';

interface DataInputFormProps {
  onSubmit: (
    data: TestData, 
    fluid: FluidProperties, 
    well: WellProperties, 
    analysisType: AnalysisType,
    defaultFlags: DefaultFlags
  ) => void;
}

const DataInputForm: React.FC<DataInputFormProps> = ({ onSubmit }) => {
  // Initial example values
  const initialValues = {
    pressure: '2500, 2750, 3000, 3200, 3350, 3450, 3500, 3525, 3540, 3550',
    time: '0.1, 1, 2, 4, 8, 16, 32, 64, 128, 256',
    flowRate: '1000, 1000, 1000, 1000, 1000, 0, 0, 0, 0, 0',
    shutInTime: '2024-03-15T08:00',
    gaugeDepth: 8500,
    preTestRates: '0,500\n24,750\n48,1000',
    cumulativeInjection: 15000,
    viscosity: 0.8,
    compressibility: 0.000012,
    formationVolumeFactor: 1.2,
    fluidDensity: 62.4,
    totalCompressibility: 0.000015,
    wellboreRadius: 0.328,
    thickness: 75,
    porosity: 0.18,
    wellboreStorage: 0.02,
    initialPressure: 4500,
    formationCompressibility: 0.000004,
    drainageRadius: 2000
  };

  const [activeTab, setActiveTab] = useState<'time-pressure' | 'fluid' | 'well'>('time-pressure');
  const [analysisType, setAnalysisType] = useState<AnalysisType>('PBU');
  const [showAdvanced, setShowAdvanced] = useState(false);
  
  // Test data state with example values
  const [pressure, setPressure] = useState<string>(initialValues.pressure);
  const [time, setTime] = useState<string>(initialValues.time);
  const [flowRate, setFlowRate] = useState<string>(initialValues.flowRate);
  const [pressureUnit, setPressureUnit] = useState<PressureUnit>('psi');
  const [timeUnit, setTimeUnit] = useState<TimeUnit>('hours');
  const [flowRateUnit, setFlowRateUnit] = useState<FlowRateUnit>('STB/day');
  
  // Advanced test data state with example values
  const [shutInTime, setShutInTime] = useState<string>(initialValues.shutInTime);
  const [gaugeDepth, setGaugeDepth] = useState<number>(initialValues.gaugeDepth);
  const [bhpCorrectionMethod, setBhpCorrectionMethod] = useState<BHPCorrectionMethod>('static');
  const [preTestRates, setPreTestRates] = useState<string>(initialValues.preTestRates);
  const [cumulativeInjection, setCumulativeInjection] = useState<number>(initialValues.cumulativeInjection);
  
  // Fluid properties state with example values
  const [viscosity, setViscosity] = useState<number>(initialValues.viscosity);
  const [viscosityUnit, setViscosityUnit] = useState<ViscosityUnit>('cP');
  const [compressibility, setCompressibility] = useState<number>(initialValues.compressibility);
  const [formationVolumeFactor, setFormationVolumeFactor] = useState<number>(initialValues.formationVolumeFactor);
  
  // Advanced fluid properties state with example values
  const [fluidDensity, setFluidDensity] = useState<number>(initialValues.fluidDensity);
  const [densityUnit, setDensityUnit] = useState<DensityUnit>('lb/ft³');
  const [totalCompressibility, setTotalCompressibility] = useState<number>(initialValues.totalCompressibility);
  const [compressibilityUnit, setCompressibilityUnit] = useState<CompressibilityUnit>('1/psi');
  
  // Well properties state with example values
  const [wellboreRadius, setWellboreRadius] = useState<number>(initialValues.wellboreRadius);
  const [thickness, setThickness] = useState<number>(initialValues.thickness);
  const [porosity, setPorosity] = useState<number>(initialValues.porosity);
  const [lengthUnit, setLengthUnit] = useState<LengthUnit>('ft');
  
  // Advanced well properties state with example values
  const [wellboreStorage, setWellboreStorage] = useState<number>(initialValues.wellboreStorage);
  const [storageUnit, setStorageUnit] = useState<StorageUnit>('bbl/psi');
  const [initialPressure, setInitialPressure] = useState<number>(initialValues.initialPressure);
  const [orientation, setOrientation] = useState<WellOrientation>('vertical');
  const [formationCompressibility, setFormationCompressibility] = useState<number>(initialValues.formationCompressibility);
  const [drainageRadius, setDrainageRadius] = useState<number>(initialValues.drainageRadius);
  const [boundaryType, setBoundaryType] = useState<BoundaryType>('no-flow');
  
  // Set all default flags to false since we're providing example values
  const [defaultFlags, setDefaultFlags] = useState<DefaultFlags>({
    viscosity: false,
    compressibility: false,
    formationVolumeFactor: false,
    wellboreRadius: false,
    thickness: false,
    porosity: false,
    fluidDensity: false,
    totalCompressibility: false,
    wellboreStorage: false,
    initialPressure: false,
    formationCompressibility: false,
    drainageRadius: false
  });
  
  const [fileUploadError, setFileUploadError] = useState<string | null>(null);

  const handleInputChange = (field: keyof DefaultFlags, value: number) => {
    setDefaultFlags(prev => ({
      ...prev,
      [field]: false
    }));
    
    switch (field) {
      case 'viscosity':
        setViscosity(value);
        break;
      case 'compressibility':
        setCompressibility(value);
        break;
      case 'formationVolumeFactor':
        setFormationVolumeFactor(value);
        break;
      case 'wellboreRadius':
        setWellboreRadius(value);
        break;
      case 'thickness':
        setThickness(value);
        break;
      case 'porosity':
        setPorosity(value);
        break;
      case 'fluidDensity':
        setFluidDensity(value);
        break;
      case 'totalCompressibility':
        setTotalCompressibility(value);
        break;
      case 'wellboreStorage':
        setWellboreStorage(value);
        break;
      case 'initialPressure':
        setInitialPressure(value);
        break;
      case 'formationCompressibility':
        setFormationCompressibility(value);
        break;
      case 'drainageRadius':
        setDrainageRadius(value);
        break;
    }
  };

  const handleClearForm = () => {
    // Reset all form fields to empty values
    setPressure('');
    setTime('');
    setFlowRate('');
    setShutInTime('');
    setGaugeDepth(0);
    setBhpCorrectionMethod('static');
    setPreTestRates('');
    setCumulativeInjection(0);
    setViscosity(1);
    setCompressibility(0.000015);
    setFormationVolumeFactor(1.2);
    setFluidDensity(62.4);
    setTotalCompressibility(0.00001);
    setWellboreRadius(0.25);
    setThickness(50);
    setPorosity(0.2);
    setWellboreStorage(0.01);
    setInitialPressure(0);
    setFormationCompressibility(0.000004);
    setDrainageRadius(1000);

    // Reset all default flags to true
    setDefaultFlags({
      viscosity: true,
      compressibility: true,
      formationVolumeFactor: true,
      wellboreRadius: true,
      thickness: true,
      porosity: true,
      fluidDensity: true,
      totalCompressibility: true,
      wellboreStorage: true,
      initialPressure: true,
      formationCompressibility: true,
      drainageRadius: true
    });
  };

  const handleRestoreExamples = () => {
    // Restore all example values
    setPressure(initialValues.pressure);
    setTime(initialValues.time);
    setFlowRate(initialValues.flowRate);
    setShutInTime(initialValues.shutInTime);
    setGaugeDepth(initialValues.gaugeDepth);
    setPreTestRates(initialValues.preTestRates);
    setCumulativeInjection(initialValues.cumulativeInjection);
    setViscosity(initialValues.viscosity);
    setCompressibility(initialValues.compressibility);
    setFormationVolumeFactor(initialValues.formationVolumeFactor);
    setFluidDensity(initialValues.fluidDensity);
    setTotalCompressibility(initialValues.totalCompressibility);
    setWellboreRadius(initialValues.wellboreRadius);
    setThickness(initialValues.thickness);
    setPorosity(initialValues.porosity);
    setWellboreStorage(initialValues.wellboreStorage);
    setInitialPressure(initialValues.initialPressure);
    setFormationCompressibility(initialValues.formationCompressibility);
    setDrainageRadius(initialValues.drainageRadius);

    // Reset all default flags to false
    setDefaultFlags({
      viscosity: false,
      compressibility: false,
      formationVolumeFactor: false,
      wellboreRadius: false,
      thickness: false,
      porosity: false,
      fluidDensity: false,
      totalCompressibility: false,
      wellboreStorage: false,
      initialPressure: false,
      formationCompressibility: false,
      drainageRadius: false
    });
  };
  
  const parseArrayInput = (input: string): number[] => {
    try {
      return input
        .split(/[\s,\t]+/)
        .map(val => val.trim())
        .filter(Boolean)
        .map(val => parseFloat(val));
    } catch (error) {
      return [];
    }
  };
  
  const parsePreTestRates = (input: string): { time: number; rate: number }[] => {
    try {
      const lines = input.split('\n');
      return lines.map(line => {
        const [time, rate] = line.split(',').map(val => parseFloat(val.trim()));
        return { time, rate };
      });
    } catch (error) {
      return [];
    }
  };
  
  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>, dataType: 'pressure' | 'time' | 'flowRate' | 'preTestRates') => {
    const file = e.target.files?.[0];
    if (!file) return;
    
    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const content = event.target?.result as string;
        
        if (dataType === 'preTestRates') {
          setPreTestRates(content);
          return;
        }
        
        const values = content
          .split(/[\r\n]+/)
          .map(line => line.trim())
          .filter(Boolean)
          .map(line => parseFloat(line));
        
        if (values.some(isNaN)) {
          setFileUploadError('File contains invalid numbers');
          return;
        }
        
        switch (dataType) {
          case 'pressure':
            setPressure(values.join(', '));
            break;
          case 'time':
            setTime(values.join(', '));
            break;
          case 'flowRate':
            setFlowRate(values.join(', '));
            break;
        }
        setFileUploadError(null);
      } catch (error) {
        setFileUploadError('Failed to parse file');
      }
    };
    reader.readAsText(file);
  };
  
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    const pressureArray = parseArrayInput(pressure);
    const timeArray = parseArrayInput(time);
    const flowRateArray = analysisType === 'DFIT' ? [] : parseArrayInput(flowRate);
    const preTestRatesArray = parsePreTestRates(preTestRates);
    
    if (pressureArray.length === 0 || timeArray.length === 0) {
      alert('Please enter valid data for pressure and time');
      return;
    }
    
    if (analysisType === 'PBU') {
      if (flowRateArray.length === 0) {
        alert('Please enter valid data for flow rate');
        return;
      }
      
      if (pressureArray.length !== timeArray.length || pressureArray.length !== flowRateArray.length) {
        alert('Pressure, time, and flow rate arrays must have the same length');
        return;
      }
    } else {
      // For DFIT, only check pressure and time
      if (pressureArray.length !== timeArray.length) {
        alert('Pressure and time arrays must have the same length');
        return;
      }
    }
    
    const testData: TestData = {
      pressure: pressureArray,
      time: timeArray,
      pressureUnit,
      timeUnit,
      shutInTime,
      gaugeDepth,
      bhpCorrectionMethod,
      preTestRates: preTestRatesArray,
      cumulativeInjection: analysisType === 'DFIT' ? cumulativeInjection : undefined
    };
    
    // Only include flow rate data for PBU analysis
    if (analysisType === 'PBU') {
      testData.flowRate = flowRateArray;
      testData.flowRateUnit = flowRateUnit;
    }
    
    const fluidProps: FluidProperties = {
      viscosity,
      viscosityUnit,
      compressibility,
      formationVolumeFactor,
      density: fluidDensity,
      densityUnit,
      totalCompressibility,
      compressibilityUnit
    };
    
    const wellProps: WellProperties = {
      wellboreRadius,
      thickness,
      porosity,
      lengthUnit,
      wellboreStorage,
      storageUnit,
      initialPressure,
      orientation,
      formationCompressibility,
      drainageRadius,
      boundaryType
    };
    
    onSubmit(testData, fluidProps, wellProps, analysisType, defaultFlags);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="flex justify-between items-center mb-4">
        <div>
          <label className="block text-sm font-medium text-gray-800 mb-2">Analysis Type</label>
          <select
            value={analysisType}
            onChange={(e) => setAnalysisType(e.target.value as AnalysisType)}
            className="w-full border-gray-300 rounded-lg shadow-sm focus:border-primary-500 focus:ring-primary-500 bg-white"
          >
            <option value="PBU">Pressure Build-Up (PBU)</option>
            <option value="DFIT">Diagnostic Fracture Injection Test (DFIT)</option>
          </select>
        </div>
        <div className="flex gap-2">
          <button
            type="button"
            onClick={handleClearForm}
            className="flex items-center px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
          >
            Clear Form
          </button>
          <button
            type="button"
            onClick={handleRestoreExamples}
            className="flex items-center px-3 py-2 text-sm font-medium text-primary-700 bg-primary-50 border border-primary-200 rounded-lg hover:bg-primary-100"
          >
            <RefreshCw size={16} className="mr-1" />
            Restore Examples
          </button>
        </div>
      </div>
      
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-4">
          {['time-pressure', 'fluid', 'well'].map((tab) => (
            <button
              key={tab}
              type="button"
              className={`py-2 px-1 border-b-2 font-medium text-sm transition-colors ${
                activeTab === tab
                  ? 'border-primary-600 text-primary-700'
                  : 'border-transparent text-gray-700 hover:text-gray-900 hover:border-gray-300'
              }`}
              onClick={() => setActiveTab(tab as 'time-pressure' | 'fluid' | 'well')}
            >
              {tab === 'time-pressure' ? 'Time & Pressure Data' : 
               tab === 'fluid' ? 'Fluid Properties' : 'Well Properties'}
            </button>
          ))}
        </nav>
      </div>

      {activeTab === 'time-pressure' && (
        <div className="space-y-4">
          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="block text-sm font-medium text-gray-700">
                Pressure Data
              </label>
              <div className="flex items-center">
                <label className="mr-2 text-sm text-gray-600">Unit:</label>
                <select
                  value={pressureUnit}
                  onChange={(e) => setPressureUnit(e.target.value as PressureUnit)}
                  className="text-sm border-gray-300 rounded-md"
                >
                  <option value="psi">psi</option>
                  <option value="bar">bar</option>
                </select>
              </div>
            </div>
            <div className="flex gap-2">
              <textarea
                value={pressure}
                onChange={(e) => setPressure(e.target.value)}
                placeholder="Enter pressure values separated by commas or spaces"
                className="flex-1 border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                rows={3}
              />
              <div className="flex flex-col justify-center">
                <label className="relative cursor-pointer bg-white rounded-md font-medium text-primary-600 hover:text-primary-500">
                  <Upload size={20} />
                  <input
                    type="file"
                    className="sr-only"
                    accept=".txt,.csv"
                    onChange={(e) => handleFileUpload(e, 'pressure')}
                  />
                </label>
              </div>
            </div>
          </div>
          
          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="block text-sm font-medium text-gray-700">
                Time Data
              </label>
              <div className="flex items-center">
                <label className="mr-2 text-sm text-gray-600">Unit:</label>
                <select
                  value={timeUnit}
                  onChange={(e) => setTimeUnit(e.target.value as TimeUnit)}
                  className="text-sm border-gray-300 rounded-md"
                >
                  <option value="hours">hours</option>
                  <option value="minutes">minutes</option>
                  <option value="seconds">seconds</option>
                </select>
              </div>
            </div>
            <div className="flex gap-2">
              <textarea
                value={time}
                onChange={(e) => setTime(e.target.value)}
                placeholder="Enter time values separated by commas or spaces"
                className="flex-1 border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                rows={3}
              />
              <div className="flex flex-col justify-center">
                <label className="relative cursor-pointer bg-white rounded-md font-medium text-primary-600 hover:text-primary-500">
                  <Upload size={20} />
                  <input
                    type="file"
                    className="sr-only"
                    accept=".txt,.csv"
                    onChange={(e) => handleFileUpload(e, 'time')}
                  />
                </label>
              </div>
            </div>
          </div>
          
          {/* Only show flow rate input for PBU analysis */}
          {analysisType === 'PBU' && (
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="block text-sm font-medium text-gray-700">
                  Flow Rate Data
                </label>
                <div className="flex items-center">
                  <label className="mr-2 text-sm text-gray-600">Unit:</label>
                  <select
                    value={flowRateUnit}
                    onChange={(e) => setFlowRateUnit(e.target.value as FlowRateUnit)}
                    className="text-sm border-gray-300 rounded-md"
                  >
                    <option value="STB/day">STB/day</option>
                    <option value="m³/day">m³/day</option>
                  </select>
                </div>
              </div>
              <div className="flex gap-2">
                <textarea
                  value={flowRate}
                  onChange={(e) => setFlowRate(e.target.value)}
                  placeholder="Enter flow rate values separated by commas or spaces"
                  className="flex-1 border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                  rows={3}
                />
                <div className="flex flex-col justify-center">
                  <label className="relative cursor-pointer bg-white rounded-md font-medium text-primary-600 hover:text-primary-500">
                    <Upload size={20} />
                    <input
                      type="file"
                      className="sr-only"
                      accept=".txt,.csv"
                      onChange={(e) => handleFileUpload(e, 'flowRate')}
                    />
                  </label>
                </div>
              </div>
            </div>
          )}
          
          {fileUploadError && (
            <div className="text-red-600 text-sm">{fileUploadError}</div>
          )}
          
          <div>
            <button
              type="button"
              onClick={() => setShowAdvanced(!showAdvanced)}
              className="flex items-center text-sm text-gray-600 hover:text-gray-900"
            >
              {showAdvanced ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
              <span className="ml-1">Advanced Options</span>
            </button>
            
            {showAdvanced && (
              <div className="mt-4 space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Shut-in Time
                  </label>
                  <input
                    type="datetime-local"
                    value={shutInTime}
                    onChange={(e) => setShutInTime(e.target.value)}
                    className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Gauge Depth (ft)
                  </label>
                  <input
                    type="number"
                    value={gaugeDepth}
                    onChange={(e) => setGaugeDepth(parseFloat(e.target.value))}
                    className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    BHP Correction Method
                  </label>
                  <select
                    value={bhpCorrectionMethod}
                    onChange={(e) => setBhpCorrectionMethod(e.target.value as BHPCorrectionMethod)}
                    className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                  >
                    <option value="static">Static</option>
                    <option value="dynamic">Dynamic</option>
                  </select>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Pre-Test Flow Rates
                  </label>
                  <div className="flex gap-2">
                    <textarea
                      value={preTestRates}
                      onChange={(e) => setPreTestRates(e.target.value)}
                      placeholder="Enter time,rate pairs (one per line)"
                      className="flex-1 border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                      rows={3}
                    />
                    <div className="flex flex-col justify-center">
                      <label className="relative cursor-pointer bg-white rounded-md font-medium text-primary-600 hover:text-primary-500">
                        <Upload size={20} />
                        <input
                          type="file"
                          className="sr-only"
                          accept=".txt,.csv"
                          onChange={(e) => handleFileUpload(e, 'preTestRates')}
                        />
                      </label>
                    </div>
                  </div>
                </div>
                
                {analysisType === 'DFIT' && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Cumulative Injection (bbl)
                    </label>
                    <input
                      type="number"
                      value={cumulativeInjection}
                      onChange={(e) => setCumulativeInjection(parseFloat(e.target.value))}
                      className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    />
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}
      
      {activeTab === 'fluid' && (
        <div className="space-y-4">
          <div>
            <div className="flex items-center mb-1">
              <label className="block text-sm font-medium text-gray-700">
                Viscosity
              </label>
              <Tooltip content="Fluid viscosity at reservoir conditions">
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </Tooltip>
            </div>
            <div className="flex gap-2">
              <input
                type="number"
                value={viscosity}
                onChange={(e) => handleInputChange('viscosity', parseFloat(e.target.value))}
                className="flex-1 border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                step="0.1"
              />
              <select
                value={viscosityUnit}
                onChange={(e) => setViscosityUnit(e.target.value as ViscosityUnit)}
                className="border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
              >
                <option value="cP">cP</option>
                <option value="Pa·s">Pa·s</option>
              </select>
            </div>
          </div>
          
          <div>
            <div className="flex items-center mb-1">
              <label className="block text-sm font-medium text-gray-700">
                Compressibility
              </label>
              <Tooltip content="Fluid compressibility at reservoir conditions">
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </Tooltip>
            </div>
            <input
              type="number"
              value={compressibility}
              onChange={(e) => handleInputChange('compressibility', parseFloat(e.target.value))}
              className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
              step="0.000001"
            />
          </div>
          
          <div>
            <div className="flex items-center mb-1">
              <label className="block text-sm font-medium text-gray-700">
                Formation Volume Factor
              </label>
              <Tooltip content="Ratio of fluid volume at reservoir conditions to surface conditions">
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </Tooltip>
            </div>
            <input
              type="number"
              value={formationVolumeFactor}
              onChange={(e) => handleInputChange('formationVolumeFactor', parseFloat(e.target.value))}
              className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
              step="0.1"
            />
          </div>
          
          <div>
            <button
              type="button"
              onClick={() => setShowAdvanced(!showAdvanced)}
              className="flex items-center text-sm text-gray-600 hover:text-gray-900"
            >
              {showAdvanced ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
              <span className="ml-1">Advanced Options</span>
            </button>
            
            {showAdvanced && (
              <div className="mt-4 space-y-4">
                <div>
                  <div className="flex items-center mb-1">
                    <label className="block text-sm font-medium text-gray-700">
                      Fluid Density
                    </label>
                    <Tooltip content="Fluid density at reservoir conditions">
                      <HelpCircle size={14} className="ml-1 text-gray-400" />
                    </Tooltip>
                  </div>
                  <div className="flex gap-2">
                    <input
                      type="number"
                      value={fluidDensity}
                      onChange={(e) => handleInputChange('fluidDensity', parseFloat(e.target.value))}
                      className="flex-1 border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                      step="0.1"
                    />
                    <select
                      value={densityUnit}
                      onChange={(e) => setDensityUnit(e.target.value as DensityUnit)}
                      className="border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    >
                      <option value="lb/ft³">lb/ft³</option>
                      <option value="kg/m³">kg/m³</option>
                    </select>
                  </div>
                </div>
                
                
                <div>
                  <div className="flex items-center mb-1">
                    <label className="block text-sm font-medium text-gray-700">
                      Total Compressibility
                    </label>
                    <Tooltip content="Combined fluid and formation compressibility">
                      <HelpCircle size={14} className="ml-1 text-gray-400" />
                    </Tooltip>
                  </div>
                  <div className="flex gap-2">
                    <input
                      type="number"
                      value={totalCompressibility}
                      onChange={(e) => handleInputChange('totalCompressibility', parseFloat(e.target.value))}
                      className="flex-1 border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                      step="0.000001"
                    />
                    <select
                      value={compressibilityUnit}
                      onChange={(e) => setCompressibilityUnit(e.target.value as CompressibilityUnit)}
                      className="border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    >
                      <option value="1/psi">1/psi</option>
                      <option value="1/bar">1/bar</option>
                    </select>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
      
      {activeTab === 'well' && (
        <div className="space-y-4">
          <div>
            <div className="flex items-center mb-1">
              <label className="block text-sm font-medium text-gray-700">
                Wellbore Radius
              </label>
              <Tooltip content="Radius of the wellbore">
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </Tooltip>
            </div>
            <div className="flex gap-2">
              <input
                type="number"
                value={wellboreRadius}
                onChange={(e) => handleInputChange('wellboreRadius', parseFloat(e.target.value))}
                className="flex-1 border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                step="0.01"
              />
              <select
                value={lengthUnit}
                onChange={(e) => setLengthUnit(e.target.value as LengthUnit)}
                className="border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
              >
                <option value="ft">ft</option>
                <option value="m">m</option>
              </select>
            </div>
          </div>
          
          <div>
            <div className="flex items-center mb-1">
              <label className="block text-sm font-medium text-gray-700">
                Formation Thickness
              </label>
              <Tooltip content="Net pay thickness of the producing formation">
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </Tooltip>
            </div>
            <div className="flex gap-2">
              <input
                type="number"
                value={thickness}
                onChange={(e) => handleInputChange('thickness', parseFloat(e.target.value))}
                className="flex-1 border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                step="1"
              />
              <select
                value={lengthUnit}
                onChange={(e) => setLengthUnit(e.target.value as LengthUnit)}
                className="border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
              >
                <option value="ft">ft</option>
                <option value="m">m</option>
              </select>
            </div>
          </div>
          
          <div>
            <div className="flex items-center mb-1">
              <label className="block text-sm font-medium text-gray-700">
                Porosity
              </label>
              <Tooltip content="Formation porosity (fraction)">
                <HelpCircle size={14} className="ml-1 text-gray-400" />
              </Tooltip>
            </div>
            <input
              type="number"
              value={porosity}
              onChange={(e) => handleInputChange('porosity', parseFloat(e.target.value))}
              className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
              step="0.01"
              min="0"
              max="1"
            />
          </div>
          
          <div>
            <button
              type="button"
              onClick={() => setShowAdvanced(!showAdvanced)}
              className="flex items-center text-sm text-gray-600 hover:text-gray-900"
            >
              {showAdvanced ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
              <span className="ml-1">Advanced Options</span>
            </button>
            
            {showAdvanced && (
              <div className="mt-4 space-y-4">
                <div>
                  <div className="flex items-center mb-1">
                    <label className="block text-sm font-medium text-gray-700">
                      Wellbore Storage
                    </label>
                    <Tooltip content="Wellbore storage coefficient">
                      <HelpCircle size={14} className="ml-1 text-gray-400" />
                    </Tooltip>
                  </div>
                  <div className="flex gap-2">
                    <input
                      type="number"
                      value={wellboreStorage}
                      onChange={(e) => handleInputChange('wellboreStorage', parseFloat(e.target.value))}
                      className="flex-1 border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                      step="0.001"
                    />
                    <select
                      value={storageUnit}
                      onChange={(e) => setStorageUnit(e.target.value as StorageUnit)}
                      className="border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    >
                      <option value="bbl/psi">bbl/psi</option>
                      <option value="ft³/psi">ft³/psi</option>
                    </select>
                  </div>
                </div>
                
                <div>
                  <div className="flex items-center mb-1">
                    <label className="block text-sm font-medium text-gray-700">
                      Initial Reservoir Pressure
                    </label>
                    <Tooltip content="Initial pressure before well test">
                      <HelpCircle size={14} className="ml-1 text-gray-400" />
                    </Tooltip>
                  </div>
                  <input
                    type="number"
                    value={initialPressure}
                    onChange={(e) => handleInputChange('initialPressure', parseFloat(e.target.value))}
                    className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    step="1"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Well Orientation
                  </label>
                  <select
                    value={orientation}
                    onChange={(e) => setOrientation(e.target.value as WellOrientation)}
                    className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                  >
                    <option value="vertical">Vertical</option>
                    <option value="horizontal">Horizontal</option>
                    <option value="multilateral">Multilateral</option>
                  </select>
                </div>
                
                <div>
                  <div className="flex items-center mb-1">
                    <label className="block text-sm font-medium text-gray-700">
                      Formation Compressibility
                    </label>
                    <Tooltip content="Rock compressibility">
                      <HelpCircle size={14} className="ml-1 text-gray-400" />
                    </Tooltip>
                  </div>
                  <input
                    type="number"
                    value={formationCompressibility}
                    onChange={(e) => handleInputChange('formationCompressibility', parseFloat(e.target.value))}
                    className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    step="0.000001"
                  />
                </div>
                
                <div>
                  <div className="flex items-center mb-1">
                    <label className="block text-sm font-medium text-gray-700">
                      Drainage Radius
                    </label>
                    <Tooltip content="Estimated radius of investigation">
                      <HelpCircle size={14} className="ml-1 text-gray-400" />
                    </Tooltip>
                  </div>
                  <div className="flex gap-2">
                    <input
                      type="number"
                      value={drainageRadius}
                      onChange={(e) => handleInputChange('drainageRadius', parseFloat(e.target.value))}
                      className="flex-1 border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                      step="1"
                    />
                    <select
                      value={lengthUnit}
                      onChange={(e) => setLengthUnit(e.target.value as LengthUnit)}
                      className="border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                    >
                      <option value="ft">ft</option>
                      <option value="m">m</option>
                    </select>
                  </div>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Boundary Type
                  </label>
                  <select
                    value={boundaryType}
                    onChange={(e) => setBoundaryType(e.target.value as BoundaryType)}
                    className="w-full border-gray-300 rounded-md shadow-sm focus:border-primary-500 focus:ring-primary-500"
                  >
                    <option value="no-flow">No Flow</option>
                    <option value="constant-pressure">Constant Pressure</option>
                  </select>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
      
      <div className="pt-4 border-t border-gray-200">
        <button
          type="submit"
          className="w-full bg-primary-700 text-white py-2 px-4 rounded-lg hover:bg-primary-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 font-medium transition-colors"
        >
          Analyze Data
        </button>
      </div>
    </form>
  );
};

export default DataInputForm;