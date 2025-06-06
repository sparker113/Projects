import React from 'react';
import { Book, MessageCircle, Video, Mail } from 'lucide-react';

function Help() {
  const resources = [
    {
      icon: <Book className="h-6 w-6" />,
      title: 'Documentation',
      description: 'Comprehensive guides and tutorials for well test analysis',
      link: '#'
    },
    {
      icon: <Video className="h-6 w-6" />,
      title: 'Video Tutorials',
      description: 'Step-by-step video guides for using the application',
      link: '#'
    },
    {
      icon: <MessageCircle className="h-6 w-6" />,
      title: 'FAQ',
      description: 'Frequently asked questions and troubleshooting tips',
      link: '#'
    },
    {
      icon: <Mail className="h-6 w-6" />,
      title: 'Contact Support',
      description: 'Get in touch with our technical support team',
      link: '#'
    }
  ];

  return (
    <div className="container mx-auto px-4 py-6">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-800 mb-8">Help Center</h1>
        
        <div className="grid gap-6 md:grid-cols-2">
          {resources.map((resource, index) => (
            <div key={index} className="bg-white rounded-lg shadow-md p-6">
              <div className="flex items-center mb-4">
                <div className="p-2 bg-blue-100 rounded-lg text-blue-600">
                  {resource.icon}
                </div>
              </div>
              
              <h3 className="text-xl font-semibold text-gray-800 mb-2">{resource.title}</h3>
              <p className="text-gray-600 mb-4">{resource.description}</p>
              
              <a
                href={resource.link}
                className="inline-flex items-center text-blue-600 hover:text-blue-800"
              >
                Learn more
                <svg
                  className="ml-2 w-4 h-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 5l7 7-7 7"
                  />
                </svg>
              </a>
            </div>
          ))}
        </div>
        
        <div className="mt-12 bg-blue-50 rounded-lg p-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Need Additional Help?</h2>
          <p className="text-gray-600 mb-4">
            Our support team is available 24/7 to help you with any questions or issues you may have.
          </p>
          <button className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors">
            Contact Support
          </button>
        </div>
      </div>
    </div>
  );
}

export default Help;