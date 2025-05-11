import { useState, useRef, useEffect } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { GeneratedResume } from "@/lib/types";

interface GeneratedResumeCardProps {
  resume: GeneratedResume;
  isSelected?: boolean;
  onClick?: () => void;
  showExpandButton?: boolean;
}

export default function GeneratedResumeCard({
  resume,
  isSelected = false,
  onClick,
  showExpandButton = true,
}: GeneratedResumeCardProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const contentRef = useRef<HTMLDivElement | null>(null);
  const [isOverflowing, setIsOverflowing] = useState(false);

  useEffect(() => {
    if (contentRef.current) {
      setIsOverflowing(contentRef.current.scrollHeight > contentRef.current.clientHeight);
    }
  }, [resume.content]);

  return (
    <Card
      className={`p-4 shadow-md rounded-xl transition-all duration-200 ${
        isSelected
          ? "border-2 border-blue-500 bg-blue-50 dark:bg-blue-900/20"
          : "border border-gray-300 dark:border-neutral-700"
      }`}
      onClick={onClick}
    >
      <CardHeader className="-mb-4">
        <CardTitle className="line-clamp-1 text-lg text-center">
          {resume.resumeTitle ? (
            resume.resumeTitle
          ) : (
            <>
              For:{" "}
              <span className="italic">
                {resume.jobDescriptionTitle
                  ? resume.jobDescriptionTitle
                  : `JobID ${resume.jobId}`}
              </span>
            </>
          )}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div 
          ref={contentRef} 
          className={`space-y-4 ${!isExpanded ? 'line-clamp-[8]' : ''}`}
        >
          {(() => {
            let parsedContent: any = null;
            try {
              parsedContent = JSON.parse(resume.content);
            } catch (err) {
              return (
                <p className="text-red-500 text-sm">
                  Failed to generate resume
                </p>
              );
            }

            return (
              <>
                {/* Education */}
                {parsedContent.educationList?.length > 0 && (
                  <div>
                    <h3 className="font-semibold text-lg mb-2">Education</h3>
                    <div className="space-y-2">
                      {parsedContent.educationList.map((edu: any, index: number) => (
                        <div key={index} className="mb-4">
                          <p className="font-medium">{edu.institution}</p>
                          <p>{edu.degree}, {edu.fieldOfStudy}</p>
                          <p className="text-sm text-muted-foreground">
                            {edu.startDate} – {edu.endDate}
                          </p>
                          <p className="text-sm">{edu.description}</p>
                          {edu.gpa && <p className="text-sm">GPA: {edu.gpa}</p>}
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Work Experience */}
                {parsedContent.workExperienceList?.length > 0 && (
                  <div>
                    <h3 className="font-semibold text-lg mb-2">Work Experience</h3>
                    <div className="space-y-2">
                      {parsedContent.workExperienceList.map((job: any, index: number) => (
                        <div key={index} className="mb-4">
                          <p className="font-medium">{job.jobTitle} at {job.company}</p>
                          <p className="text-sm text-muted-foreground">
                            {job.startDate} – {job.endDate}
                          </p>
                          <p className="text-sm whitespace-pre-line">{job.description}</p>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Skills */}
                {parsedContent.skills?.length > 0 && (
                  <div>
                    <h3 className="font-semibold text-lg mb-2">Skills</h3>
                    <div className="flex flex-wrap gap-2">
                      {parsedContent.skills.map((skill: string, index: number) => (
                        <span
                          key={index}
                          className="px-2 py-1 bg-gray-200 dark:bg-neutral-700 rounded-full text-sm"
                        >
                          {skill}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </>
            );
          })()}
        </div>
        {showExpandButton && (isOverflowing || isExpanded) && (
          <Button
            variant="link"
            className="p-0 h-auto text-sm mt-2"
            onClick={(e) => {
              e.stopPropagation();
              setIsExpanded(!isExpanded);
            }}
          >
            {isExpanded ? "Show less" : "Read more"}
          </Button>
        )}
      </CardContent>
      <CardFooter className="-mt-2 justify-between text-xs text-muted-foreground items-center">
        <p>
          Last Updated: {new Date(resume.updatedAt).toLocaleString()}
        </p>
        <p>GenID: {resume.resumeId}</p>
      </CardFooter>
    </Card>
  );
}