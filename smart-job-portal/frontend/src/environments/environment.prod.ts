// Smart environment detection
const getApiUrl = (): string => {
  // Check if running locally
  if (typeof window !== 'undefined') {
    const hostname = window.location.hostname;
    
    // Local development
    if (hostname === 'localhost' || hostname === '127.0.0.1' || hostname.startsWith('192.168.')) {
      return 'http://localhost:8080';
    }
    
    // Production deployment
    return 'https://talentai-portal.onrender.com';
  }
  
  // Fallback for SSR
  return 'https://talentai-portal.onrender.com';
};

export const environment = {
  production: true,
  apiUrl: getApiUrl()
};
