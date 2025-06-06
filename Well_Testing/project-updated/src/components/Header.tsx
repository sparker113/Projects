import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { BarChart2, Menu, X } from 'lucide-react';

const Header: React.FC = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const location = useLocation();

  const isActive = (path: string) => {
    return location.pathname === path ? 'text-white bg-primary-800' : '';
  };

  const navLinks = [
    { path: '/', label: 'Dashboard' },
    { path: '/analysis', label: 'Analysis' },
    { path: '/reports', label: 'Reports' },
    { path: '/help', label: 'Help' }
  ];

  return (
    <header className="bg-primary-950 text-white shadow-soft">
      <div className="container mx-auto px-4 py-4">
        <div className="flex justify-between items-center">
          <Link to="/" className="flex items-center space-x-3 group">
            <BarChart2 size={28} className="text-primary-200 group-hover:text-white transition-colors" />
            <h1 className="text-2xl font-semibold tracking-tight">WellTest Analyzer</h1>
          </Link>
          
          <button 
            onClick={() => setIsMenuOpen(!isMenuOpen)}
            className="md:hidden p-2 hover:bg-primary-800 rounded-lg transition-colors"
            aria-label="Toggle menu"
          >
            {isMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
          
          <nav className="hidden md:block">
            <ul className="flex space-x-2">
              {navLinks.map(({ path, label }) => (
                <li key={path}>
                  <Link 
                    to={path}
                    className={`px-3 py-2 rounded-lg transition-colors text-sm font-medium hover:bg-primary-800 hover:text-white ${isActive(path)}`}
                  >
                    {label}
                  </Link>
                </li>
              ))}
            </ul>
          </nav>
        </div>
        
        {/* Mobile menu */}
        {isMenuOpen && (
          <nav className="md:hidden mt-4 pb-2 border-t border-primary-800">
            <ul className="space-y-1 pt-3">
              {navLinks.map(({ path, label }) => (
                <li key={path}>
                  <Link
                    to={path}
                    className={`block py-2 px-3 rounded-lg hover:bg-primary-800 transition-colors ${isActive(path)}`}
                    onClick={() => setIsMenuOpen(false)}
                  >
                    {label}
                  </Link>
                </li>
              ))}
            </ul>
          </nav>
        )}
      </div>
    </header>
  );
};

export default Header;