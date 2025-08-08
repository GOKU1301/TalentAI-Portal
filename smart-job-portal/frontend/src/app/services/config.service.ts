import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  private _apiUrl: string;
  private _isLocal: boolean;

  constructor() {
    this._apiUrl = this.determineApiUrl();
    this._isLocal = this.isRunningLocally();
    
    // Debug logging
    console.log('🔧 Config Service Initialized:');
    console.log(`   Environment: ${environment.production ? 'Production' : 'Development'}`);
    console.log(`   Running locally: ${this._isLocal}`);
    console.log(`   API URL: ${this._apiUrl}`);
    console.log(`   Frontend URL: ${this.getFrontendUrl()}`);
  }

  private determineApiUrl(): string {
    // Check if we're in browser environment
    if (typeof window !== 'undefined') {
      const hostname = window.location.hostname;
      
      // Local development patterns
      if (hostname === 'localhost' || 
          hostname === '127.0.0.1' || 
          hostname.startsWith('192.168.') ||
          hostname.startsWith('10.') ||
          hostname.endsWith('.local')) {
        return 'http://localhost:8080';
      }
      
      // Production deployment
      return 'https://talentai-portal.onrender.com';
    }
    
    // SSR fallback - use environment default
    return environment.production ? 
      'https://talentai-portal.onrender.com' : 
      'http://localhost:8080';
  }

  private isRunningLocally(): boolean {
    if (typeof window !== 'undefined') {
      const hostname = window.location.hostname;
      return hostname === 'localhost' || 
             hostname === '127.0.0.1' || 
             hostname.startsWith('192.168.') ||
             hostname.startsWith('10.') ||
             hostname.endsWith('.local');
    }
    return !environment.production;
  }

  private getFrontendUrl(): string {
    if (typeof window !== 'undefined') {
      return window.location.origin;
    }
    return 'unknown';
  }

  get apiUrl(): string {
    return this._apiUrl;
  }

  get isLocal(): boolean {
    return this._isLocal;
  }

  // Method to manually override API URL if needed
  setApiUrl(url: string): void {
    console.log(`🔧 API URL manually overridden: ${this._apiUrl} → ${url}`);
    this._apiUrl = url;
  }

  // Get full API endpoint URL
  getApiEndpoint(path: string): string {
    // Remove leading slash if present to avoid double slashes
    const cleanPath = path.startsWith('/') ? path.substring(1) : path;
    return `${this._apiUrl}/${cleanPath}`;
  }

  // Debug method to log current configuration
  logConfig(): void {
    console.log('🔧 Current Configuration:');
    console.log(`   API URL: ${this._apiUrl}`);
    console.log(`   Is Local: ${this._isLocal}`);
    console.log(`   Environment Production: ${environment.production}`);
    console.log(`   Frontend URL: ${this.getFrontendUrl()}`);
  }
}
