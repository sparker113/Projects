import { PressureUnit, TimeUnit, FlowRateUnit, LengthUnit, ViscosityUnit } from '../types/wellTest';

// Conversion factors to standard units (field units)
const pressureConversion: Record<PressureUnit, number> = {
  'psi': 1,
  'bar': 14.5038
};

const timeConversion: Record<TimeUnit, number> = {
  'hours': 1,
  'minutes': 1/60,
  'seconds': 1/3600
};

const flowRateConversion: Record<FlowRateUnit, number> = {
  'STB/day': 1,
  'm³/day': 6.28981
};

const lengthConversion: Record<LengthUnit, number> = {
  'ft': 1,
  'm': 3.28084
};

const viscosityConversion: Record<ViscosityUnit, number> = {
  'cP': 1,
  'Pa·s': 1000
};

export function convertPressure(value: number, from: PressureUnit, to: PressureUnit): number {
  return value * pressureConversion[from] / pressureConversion[to];
}

export function convertTime(value: number, from: TimeUnit, to: TimeUnit): number {
  return value * timeConversion[from] / timeConversion[to];
}

export function convertFlowRate(value: number, from: FlowRateUnit, to: FlowRateUnit): number {
  return value * flowRateConversion[from] / flowRateConversion[to];
}

export function convertLength(value: number, from: LengthUnit, to: LengthUnit): number {
  return value * lengthConversion[from] / lengthConversion[to];
}

export function convertViscosity(value: number, from: ViscosityUnit, to: ViscosityUnit): number {
  return value * viscosityConversion[from] / viscosityConversion[to];
}

export function convertArrayToFieldUnits(
  values: number[],
  fromUnit: PressureUnit | TimeUnit | FlowRateUnit | LengthUnit | ViscosityUnit,
  type: 'pressure' | 'time' | 'flowRate' | 'length' | 'viscosity'
): number[] {
  const conversionFunction = {
    'pressure': (v: number) => convertPressure(v, fromUnit as PressureUnit, 'psi'),
    'time': (v: number) => convertTime(v, fromUnit as TimeUnit, 'hours'),
    'flowRate': (v: number) => convertFlowRate(v, fromUnit as FlowRateUnit, 'STB/day'),
    'length': (v: number) => convertLength(v, fromUnit as LengthUnit, 'ft'),
    'viscosity': (v: number) => convertViscosity(v, fromUnit as ViscosityUnit, 'cP')
  }[type];

  return values.map(conversionFunction);
}

// Physical constants in field units
export const physicalConstants = {
  gravitationalAcceleration: 32.174, // ft/s²
  standardPressure: 14.7, // psi
  standardTemperature: 60, // °F
  gasConstant: 10.732, // (psi·ft³)/(lbmol·°R)
};

// Validation ranges for physical parameters
export const validationRanges = {
  pressure: {
    min: 0,
    max: 20000, // psi
    validate: (value: number) => value >= 0 && value <= 20000
  },
  temperature: {
    min: 32,
    max: 400, // °F
    validate: (value: number) => value >= 32 && value <= 400
  },
  porosity: {
    min: 0,
    max: 1,
    validate: (value: number) => value > 0 && value < 1
  },
  permeability: {
    min: 0.0001,
    max: 10000, // mD
    validate: (value: number) => value > 0.0001 && value <= 10000
  },
  viscosity: {
    min: 0.01,
    max: 1000, // cP
    validate: (value: number) => value >= 0.01 && value <= 1000
  }
};