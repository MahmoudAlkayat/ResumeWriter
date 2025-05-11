export interface GeneratedResume {
    resumeId: string;
    content: string;
    createdAt: string;
    updatedAt: string;
    jobId: string;
    jobDescriptionTitle: string | null;
    resumeTitle: string | null;
}

export interface JobDescription {
    jobId: string;
    title?: string;
    text: string;
    submittedAt: string;
}