"use client";

import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import { Card, CardContent } from "@/components/ui/card";
import { useAuth } from "@/hooks/auth";
import { useToast } from "@/contexts/ToastProvider";
import LoadingScreen from "@/components/LoadingScreen";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Label } from "@/components/ui/label";

interface Template {
  templateId: string;
  name: string;
  description: string;
  previewUrl?: string;
}

interface AuthUser {
  id: string;
  accessToken?: string; // Changed to match your actual user type
}

export default function TemplateSelector({ resumeId, onFormat }: { 
  resumeId: string; 
  onFormat: (templateId: string) => void 
}) {
  const { user } = useAuth() as { user: AuthUser | null };
  const { showError, showSuccess } = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [templates, setTemplates] = useState<Template[]>([]);
  const [selectedTemplate, setSelectedTemplate] = useState<string>("");
  const [isFormatting, setIsFormatting] = useState(false);

  async function fetchTemplates() {
    if (!user?.id) return;
    
    try {
      setIsLoading(true);
      const response = await fetch(`/api/templates`, {
        credentials: "include",
        headers: {
          Authorization: `Bearer ${user.accessToken}`, // Using accessToken now
        },
      });

      if (!response.ok) {
        const errText = await response.text();
        throw new Error(errText || "Failed to fetch templates");
      }

      const data = await response.json();
      setTemplates(data.templates || []);
      if (data.templates?.length > 0) {
        setSelectedTemplate(data.templates[0].templateId);
      }
    } catch (err) {
      if (err instanceof Error) showError(err.message);
      else showError("An unknown error occurred");
    } finally {
      setIsLoading(false);
    }
  }

  const handleFormat = async () => {
    if (!selectedTemplate || !resumeId) {
      showError("Please select a template and ensure resume is loaded");
      return;
    }

    try {
      setIsFormatting(true);
      const response = await fetch(`/api/resumes/format`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${user?.accessToken}`, // Using accessToken now
        },
        body: JSON.stringify({
          resumeId,
          templateId: selectedTemplate
        }),
      });

      if (!response.ok) {
        const errText = await response.text();
        throw new Error(errText || "Failed to format resume");
      }

      const data = await response.json();
      showSuccess("Resume formatted successfully!");
      onFormat(data.formattedResumeId);
    } catch (err) {
      if (err instanceof Error) showError(err.message);
      else showError("An unknown error occurred");
    } finally {
      setIsFormatting(false);
    }
  };

  useEffect(() => {
    fetchTemplates();
  }, [user?.id]);

  if (isLoading) {
    return <LoadingScreen />;
  }

  return (
    <Background className="relative flex flex-col items-center justify-start min-h-screen p-8 text-center">
      <h2 className="text-4xl font-bold text-primary mb-8 drop-shadow-md">
        Select Resume Template
      </h2>

      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8 dark:bg-neutral-900 dark:border-neutral-800">
        {templates.length === 0 ? (
          <p className="text-xl text-foreground drop-shadow-sm text-center">
            No templates available.
          </p>
        ) : (
          <>
            <RadioGroup 
              value={selectedTemplate} 
              onValueChange={setSelectedTemplate}
              className="grid gap-6 md:grid-cols-2 lg:grid-cols-3"
            >
              {templates.map((template) => (
                <Card
                  key={template.templateId}
                  className={`p-6 cursor-pointer transition-all ${
                    selectedTemplate === template.templateId
                      ? "border-2 border-blue-500 dark:border-blue-600 bg-blue-50 dark:bg-blue-900/20"
                      : "hover:border-gray-400 dark:hover:border-neutral-600"
                  }`}
                  onClick={() => setSelectedTemplate(template.templateId)}
                >
                  <CardContent className="flex flex-col items-center gap-4 p-0">
                    {template.previewUrl && (
                      <div className="w-full h-40 bg-gray-100 dark:bg-neutral-800 rounded-md overflow-hidden">
                        <img 
                          src={template.previewUrl} 
                          alt={template.name}
                          className="w-full h-full object-contain"
                        />
                      </div>
                    )}
                    <div className="w-full text-center">
                      <Label className="text-lg font-semibold text-primary cursor-pointer">
                        {template.name}
                      </Label>
                      <p className="text-sm text-gray-600 dark:text-gray-300 mt-2">
                        {template.description}
                      </p>
                    </div>
                    <RadioGroupItem 
                      value={template.templateId} 
                      id={template.templateId}
                      className="mt-2"
                    />
                  </CardContent>
                </Card>
              ))}
            </RadioGroup>

            <div className="mt-8 flex justify-center">
              <Button
                onClick={handleFormat}
                disabled={isFormatting || !selectedTemplate}
                className="bg-green-600 hover:bg-green-700 text-white px-8 py-4 text-lg"
              >
                {isFormatting ? (
                  <span className="flex items-center gap-2">
                    <span className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></span>
                    Formatting...
                  </span>
                ) : (
                  "Format Resume"
                )}
              </Button>
            </div>
          </>
        )}
      </div>
    </Background>
  );
}