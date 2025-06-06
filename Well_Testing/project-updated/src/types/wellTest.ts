export type PressureUnit = 'psi' | 'bar';
export type TimeUnit = 'hours' | 'minutes' | 'seconds';
export type FlowRateUnit = 'STB/day' | 'm³/day';
export type PermeabilityUnit = 'mD' | 'D';
export type LengthUnit = 'ft' | 'm';
export type VolumeUnit = 'STB' | 'm³' | 'ft³';
export type ViscosityUnit = 'cP' | 'Pa·s';
export type DensityUnit = 'lb/ft³' | 'kg/m³';
export type CompressibilityUnit = '1/psi' | '1/bar';
export type StorageUnit = 'bbl/psi' | 'ft³/psi';
export type AreaUnit = 'ft²' | 'acres';

export type AnalysisType = 'PBU' | 'DFIT';

export type FlowRegime = 
  | 'radial_flow' 
  | 'linear_flow' 
  | 'spherical_flow' 
  | 'boundary_dominated' 
  | 'wellbore_storage'
  | 'dual_porosity'
  | 'bilinear_flow'
  | 'channel_flow'
  | 'partial_penetration'
  | 'naturally_fractured'
  | 'composite'
  | 'unknown';

export type WellOrientation = 'vertical' | 'horizontal' | 'multilateral';
export type BoundaryType = 'no-flow' | 'constant-pressure';
export type BHPCorrectionMethod = 'static' | 'dynamic';

export interface TestData {
  pressure: number[];
  time: number[];
  flowRate?: number[];  // Made optional for DFIT analysis
  pressureUnit: PressureUnit;
  timeUnit: TimeUnit;
  flowRateUnit?: FlowRateUnit;  // Made optional for DFIT analysis
  shutInTime?: string;
  gaugeDepth?: number;
  bhpCorrectionMethod?: BHPCorrectionMethod;
  preTestRates?: { time: number; rate: number }[];
  cumulativeInjection?: number;
}

export interface FluidProperties {
  viscosity: number;
  viscosityUnit: ViscosityUnit;
  compressibility: number;
  formationVolumeFactor: number;
  density?: number;
  densityUnit?: DensityUnit;
  totalCompressibility?: number;
  compressibilityUnit?: CompressibilityUnit;
}

export interface WellProperties {
  wellboreRadius: number;
  thickness: number;
  porosity: number;
  lengthUnit: LengthUnit;
  wellboreStorage?: number;
  storageUnit?: StorageUnit;
  initialPressure?: number;
  orientation?: WellOrientation;
  formationCompressibility?: number;
  drainageRadius?: number;
  boundaryType?: BoundaryType;
}

export interface TransientFlowResults {
  permeability: {
    value: number;
    unit: PermeabilityUnit;
    confidenceInterval: [number, number];
  };
  skinFactor: {
    value: number;
    confidenceInterval: [number, number];
  };
  initialReservoirPressure: {
    value: number;
    unit: PressureUnit;
    confidenceInterval: [number, number];
  };
  wellboreStorageCoefficient: {
    value: number;
    confidenceInterval: [number, number];
  };
}

export interface BoundaryFlowResults {
  calculatedDrainageRadius: {
    value: number;
    unit: LengthUnit;
    confidenceInterval: [number, number];
  };
  connectedPoreVolume: {
    value: number;
    unit: VolumeUnit;
    confidenceInterval: [number, number];
  };
  averageReservoirPressure: {
    value: number;
    unit: PressureUnit;
    confidenceInterval: [number, number];
  };
  drainageArea: {
    value: number;
    unit: AreaUnit;
    confidenceInterval: [number, number];
  };
}

export interface DFITResults {
  isip: {
    value: number;
    unit: PressureUnit;
    confidenceInterval: [number, number];
  };
  closurePressure: {
    value: number;
    unit: PressureUnit;
    confidenceInterval: [number, number];
  };
  minStress: {
    value: number;
    unit: PressureUnit;
    confidenceInterval: [number, number];
  };
  leakOffCoefficient: {
    value: number;
    unit: 'ft/√min';
    confidenceInterval: [number, number];
  };
  permeability: {
    value: number;
    unit: PermeabilityUnit;
    confidenceInterval: [number, number];
  };
  fracturePressure: {
    value: number;
    unit: PressureUnit;
    confidenceInterval: [number, number];
  };
  processZone: {
    value: number;
    unit: PressureUnit;
    confidenceInterval: [number, number];
  };
}

export interface AnalysisResults {
  transient?: TransientFlowResults;
  boundary?: BoundaryFlowResults;
  dfit?: DFITResults;
  flowRegime?: FlowRegime;
  qualityIndicators: {
    regressionQuality: number;
    dataQuality: number;
    interpretationConfidence: number;
  };
}

export interface DefaultFlags {
  viscosity: boolean;
  compressibility: boolean;
  formationVolumeFactor: boolean;
  wellboreRadius: boolean;
  thickness: boolean;
  porosity: boolean;
  fluidDensity: boolean;
  totalCompressibility: boolean;
  wellboreStorage: boolean;
  initialPressure: boolean;
  formationCompressibility: boolean;
  drainageRadius: boolean;
}

export interface AnalysisResultsProps {
  results: any;
  flowRegime: FlowRegime | null;
  onManualRegimeChange: (regime: FlowRegime) => void;
  defaultFlags: DefaultFlags;
}

export interface DFITAnalysisResultsProps {
  results: DFITResults;
  defaultFlags: DefaultFlags;
}