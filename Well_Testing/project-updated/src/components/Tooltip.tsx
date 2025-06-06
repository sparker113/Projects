import React, { useState } from 'react';

interface TooltipProps {
  content: string;
  children: React.ReactNode;
}

const Tooltip: React.FC<TooltipProps> = ({ content, children }) => {
  const [isVisible, setIsVisible] = useState(false);
  
  return (
    <span className="relative inline-block">
      <span
        onMouseEnter={() => setIsVisible(true)}
        onMouseLeave={() => setIsVisible(false)}
        className="inline-block cursor-help"
      >
        {children}
      </span>
      
      {isVisible && (
        <span className="absolute z-10 px-3 py-2 text-xs font-normal text-white bg-gray-800 rounded-md shadow-md transition-opacity duration-300 w-48 -left-20 bottom-full mb-2">
          {content}
          <span className="absolute -bottom-1 left-1/2 transform -translate-x-1/2 w-2 h-2 bg-gray-800 rotate-45"></span>
        </span>
      )}
    </span>
  );
};

export default Tooltip;