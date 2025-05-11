import { useState, useRef, useEffect } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { JobDescription } from "@/lib/types";

interface JobDescriptionCardProps {
  job: JobDescription;
  isSelected?: boolean;
  onClick?: () => void;
  showExpandButton?: boolean;
}

export default function JobDescriptionCard({
  job,
  isSelected = false,
  onClick,
  showExpandButton = true,
}: JobDescriptionCardProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const contentRef = useRef<HTMLParagraphElement | null>(null);
  const [isOverflowing, setIsOverflowing] = useState(false);

  useEffect(() => {
    if (contentRef.current) {
      setIsOverflowing(contentRef.current.scrollHeight > contentRef.current.clientHeight);
    }
  }, [job.text]);

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
          {job.title}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <p
          ref={contentRef}
          className={`text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap break-words ${
            !isExpanded ? "line-clamp-4" : ""
          }`}
        >
          {job.text}
        </p>
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
          Submitted: {new Date(job.submittedAt).toLocaleString()}
        </p>
        <p>JobID: {job.jobId}</p>
      </CardFooter>
    </Card>
  );
} 