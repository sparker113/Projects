export type Json =
  | string
  | number
  | boolean
  | null
  | { [key: string]: Json | undefined }
  | Json[]

export interface Database {
  public: {
    Tables: {
      well_tests: {
        Row: {
          id: string
          user_id: string | null
          well_name: string
          test_type: string
          test_date: string
          status: string
          created_at: string | null
          updated_at: string | null
        }
        Insert: {
          id?: string
          user_id?: string | null
          well_name: string
          test_type: string
          test_date?: string
          status?: string
          created_at?: string | null
          updated_at?: string | null
        }
        Update: {
          id?: string
          user_id?: string | null
          well_name?: string
          test_type?: string
          test_date?: string
          status?: string
          created_at?: string | null
          updated_at?: string | null
        }
      }
      test_data: {
        Row: {
          id: string
          well_test_id: string | null
          pressure: number[]
          time: number[]
          flow_rate: number[]
          pressure_unit: string
          time_unit: string
          flow_rate_unit: string
          shut_in_time: string | null
          gauge_depth: number | null
          bhp_correction_method: string | null
          pre_test_rates: Json | null
          cumulative_injection: number | null
          created_at: string | null
          updated_at: string | null
        }
        Insert: {
          id?: string
          well_test_id?: string | null
          pressure: number[]
          time: number[]
          flow_rate: number[]
          pressure_unit: string
          time_unit: string
          flow_rate_unit: string
          shut_in_time?: string | null
          gauge_depth?: number | null
          bhp_correction_method?: string | null
          pre_test_rates?: Json | null
          cumulative_injection?: number | null
          created_at?: string | null
          updated_at?: string | null
        }
        Update: {
          id?: string
          well_test_id?: string | null
          pressure?: number[]
          time?: number[]
          flow_rate?: number[]
          pressure_unit?: string
          time_unit?: string
          flow_rate_unit?: string
          shut_in_time?: string | null
          gauge_depth?: number | null
          bhp_correction_method?: string | null
          pre_test_rates?: Json | null
          cumulative_injection?: number | null
          created_at?: string | null
          updated_at?: string | null
        }
      }
      fluid_properties: {
        Row: {
          id: string
          well_test_id: string | null
          viscosity: number
          viscosity_unit: string
          compressibility: number
          formation_volume_factor: number
          density: number | null
          density_unit: string | null
          total_compressibility: number | null
          compressibility_unit: string | null
          created_at: string | null
          updated_at: string | null
        }
        Insert: {
          id?: string
          well_test_id?: string | null
          viscosity: number
          viscosity_unit: string
          compressibility: number
          formation_volume_factor: number
          density?: number | null
          density_unit?: string | null
          total_compressibility?: number | null
          compressibility_unit?: string | null
          created_at?: string | null
          updated_at?: string | null
        }
        Update: {
          id?: string
          well_test_id?: string | null
          viscosity?: number
          viscosity_unit?: string
          compressibility?: number
          formation_volume_factor?: number
          density?: number | null
          density_unit?: string | null
          total_compressibility?: number | null
          compressibility_unit?: string | null
          created_at?: string | null
          updated_at?: string | null
        }
      }
      well_properties: {
        Row: {
          id: string
          well_test_id: string | null
          wellbore_radius: number
          thickness: number
          porosity: number
          length_unit: string
          wellbore_storage: number | null
          storage_unit: string | null
          initial_pressure: number | null
          orientation: string | null
          formation_compressibility: number | null
          drainage_radius: number | null
          boundary_type: string | null
          created_at: string | null
          updated_at: string | null
        }
        Insert: {
          id?: string
          well_test_id?: string | null
          wellbore_radius: number
          thickness: number
          porosity: number
          length_unit: string
          wellbore_storage?: number | null
          storage_unit?: string | null
          initial_pressure?: number | null
          orientation?: string | null
          formation_compressibility?: number | null
          drainage_radius?: number | null
          boundary_type?: string | null
          created_at?: string | null
          updated_at?: string | null
        }
        Update: {
          id?: string
          well_test_id?: string | null
          wellbore_radius?: number
          thickness?: number
          porosity?: number
          length_unit?: string
          wellbore_storage?: number | null
          storage_unit?: string | null
          initial_pressure?: number | null
          orientation?: string | null
          formation_compressibility?: number | null
          drainage_radius?: number | null
          boundary_type?: string | null
          created_at?: string | null
          updated_at?: string | null
        }
      }
    }
    Views: {
      [_ in never]: never
    }
    Functions: {
      [_ in never]: never
    }
    Enums: {
      [_ in never]: never
    }
  }
}