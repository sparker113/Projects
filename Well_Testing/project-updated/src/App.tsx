import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import Header from './components/Header';
import Dashboard from './pages/Dashboard';
import Analysis from './pages/Analysis';
import Reports from './pages/Reports';
import Help from './pages/Help';
import Auth from './components/Auth';
import WellTestDetailsPage from './pages/WellTestDetailsPage';
import { useAuth } from './contexts/AuthContext';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="animate-spin rounded-full h-8 w-8 border-4 border-primary-600 border-t-transparent"></div>
      </div>
    );
  }

  if (!user) {
    return <Auth />;
  }

  return <>{children}</>;
}

function App() {
  return (
    <AuthProvider>
      <div className="min-h-screen bg-gray-100 flex flex-col">
        <Header />
        <main className="flex-1 overflow-hidden">
          <Routes>
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/analysis"
              element={
                <ProtectedRoute>
                  <Analysis />
                </ProtectedRoute>
              }
            />
            <Route
              path="/analysis/:id"
              element={
                <ProtectedRoute>
                  <WellTestDetailsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/reports"
              element={
                <ProtectedRoute>
                  <Reports />
                </ProtectedRoute>
              }
            />
            <Route
              path="/reports/:id"
              element={
                <ProtectedRoute>
                  <WellTestDetailsPage />
                </ProtectedRoute>
              }
            />
            <Route path="/help" element={<Help />} />
          </Routes>
        </main>
      </div>
    </AuthProvider>
  );
}

export default App