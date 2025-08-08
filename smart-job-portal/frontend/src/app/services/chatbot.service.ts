import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConfigService } from './config.service';

export interface ChatRequest {
  message: string;
}

export interface ChatResponse {
  response: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private get apiUrl(): string {
    return this.configService.getApiEndpoint('api/chatbot');
  }

  constructor(
    private http: HttpClient,
    private configService: ConfigService
  ) { }

  /**
   * Send a message to the chatbot and get a response
   * @param message The user's message
   * @returns Observable with the chatbot's response
   */
  sendMessage(message: string): Observable<ChatResponse> {
    const request: ChatRequest = { message };
    return this.http.post<ChatResponse>(this.apiUrl, request);
  }
}
