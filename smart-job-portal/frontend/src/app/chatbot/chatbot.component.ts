import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService } from '../services/chatbot.service';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent implements OnInit {
  isChatOpen = false;
  isLoading = false;
  userMessage = '';
  chatHistory: { sender: 'user' | 'bot', message: string }[] = [];

  constructor(private chatbotService: ChatbotService) { }

  ngOnInit(): void {
    // Initialize with a welcome message
    this.chatHistory.push({
      sender: 'bot',
      message: 'Hi there! I\'m your job search assistant. How can I help you today?'
    });
  }

  toggleChat(): void {
    this.isChatOpen = !this.isChatOpen;
  }

  sendMessage(): void {
    if (!this.userMessage.trim()) return;
    
    // Add user message to chat history
    this.chatHistory.push({
      sender: 'user',
      message: this.userMessage
    });
    
    const message = this.userMessage;
    this.userMessage = ''; // Clear input field
    this.isLoading = true;
    
    // Call service to get response
    this.chatbotService.sendMessage(message).subscribe({
      next: (response) => {
        this.chatHistory.push({
          sender: 'bot',
          message: response.response
        });
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error getting chatbot response', error);
        this.chatHistory.push({
          sender: 'bot',
          message: 'Sorry, I encountered an error. Please try again later.'
        });
        this.isLoading = false;
      }
    });
  }
}
