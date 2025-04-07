"use client";

import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { useToast } from './ToastProvider';

interface ResumeProcessingContextType {
    activeResumeId: number | null;
    setActiveResumeId: (id: number | null) => void;
}

const ResumeProcessingContext = createContext<ResumeProcessingContextType | undefined>(undefined);

export function ResumeProcessingProvider({ children }: { children: React.ReactNode }) {
    const [activeResumeId, setActiveResumeId] = useState<number | null>(null);
    const { showSuccess, showError } = useToast();

    useEffect(() => {
        if (!activeResumeId) return;

        console.log('Connecting to SSE for resumeId:', activeResumeId);
        const eventSource = new EventSource(`http://localhost:8080/api/resumes/${activeResumeId}/status`);

        eventSource.onopen = () => {
            console.log('SSE connection opened');
        };

        eventSource.addEventListener('processing-complete', (event) => {
            console.log('Received processing-complete event:', event.data);
            try {
                const data = JSON.parse(event.data);
                showSuccess('Resume processed!');
                setActiveResumeId(null); // Clear the active resume ID after completion
            } catch (error) {
                console.error('Error parsing SSE data:', error);
                showError('Error processing resume data');
            }
            eventSource.close();
        });

        eventSource.onerror = (error) => {
            console.error('SSE Error:', error);
            if (eventSource.readyState === EventSource.CLOSED) {
                console.log('SSE connection closed');
                showError('Connection to server lost. Please refresh the page.');
            } else {
                showError('Failed to process resume. Please try again.');
            }
            eventSource.close();
        };

        return () => {
            console.log('Cleaning up SSE connection');
            eventSource.close();
        };
    }, [activeResumeId, showSuccess, showError]);

    return (
        <ResumeProcessingContext.Provider value={{ activeResumeId, setActiveResumeId }}>
            {children}
        </ResumeProcessingContext.Provider>
    );
}

export function useResumeProcessing() {
    const context = useContext(ResumeProcessingContext);
    if (context === undefined) {
        throw new Error('useResumeProcessing must be used within a ResumeProcessingProvider');
    }
    return context;
} 