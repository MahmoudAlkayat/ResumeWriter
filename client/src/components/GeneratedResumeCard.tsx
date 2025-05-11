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
  const contentRef = useRef<HTMLPreElement | null>(null);
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
        <CardTitle className="line-clamp-1 text-lg">
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
        {!resume.content && <p className="text-red-500">Failed to generate</p>}
        <pre
          ref={contentRef}
          className={`text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap break-words ${
            !isExpanded ? "line-clamp-4" : ""
          }`}
        >
          {(() => {
            try {
              return JSON.stringify(JSON.parse(resume.content), null, 2);
            } catch {
              return resume.content;
            }
          })()}
        </pre>
        {showExpandButton && isOverflowing && (
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
      <CardFooter className="-mt-2">
        <p className="text-sm text-muted-foreground">
          Last Updated: {new Date(resume.updatedAt).toLocaleString()}
        </p>
      </CardFooter>
    </Card>
  );
}