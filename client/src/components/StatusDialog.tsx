import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import { CircleEllipsis, Loader2, CheckCircle, XCircle } from "lucide-react";
import { DialogHeader } from "./ui/dialog";
import { useState, useEffect } from "react";
import { Button } from "./ui/button";
import { useToast } from "@/contexts/ToastProvider";

interface Status {
  id: string;
  type: "UPLOADED_RESUME" | "GENERATED_RESUME" | "FREEFORM_ENTRY";
  resumeName?: string;
  jobTitle?: string;
  status: "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED";
  startedAt: string;
  completedAt?: string;
  error?: string;
}

interface StatusDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function StatusDialog({ open, onOpenChange }: StatusDialogProps) {
  const [statuses, setStatuses] = useState<Status[]>([]);
  const { showError } = useToast();

  const fetchStatuses = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/resumes/status", {
        credentials: "include",
      });
      if (!response.ok) throw new Error("Failed to fetch statuses");
      const data = await response.json();
      setStatuses(data);
    } catch (error) {
      showError("Failed to fetch status updates");
      console.error("Error fetching statuses:", error);
    }
  };

  useEffect(() => {
    if (open) {
      fetchStatuses();
    }
  }, [open]);

  // Poll for updates every 2 seconds if there are processing items
  useEffect(() => {
    if (!open || !statuses.some(s => s.status === "PROCESSING")) return;
    
    const interval = setInterval(fetchStatuses, 2000);
    return () => clearInterval(interval);
  }, [open, statuses]);

  const getStatusIcon = (status: Status["status"]) => {
    switch (status) {
      case "PROCESSING":
        return <Loader2 className="h-4 w-4 animate-spin text-blue-500" />;
      case "COMPLETED":
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case "FAILED":
        return <XCircle className="h-4 w-4 text-red-500" />;
      default:
        return <CircleEllipsis className="h-4 w-4 text-gray-500" />;
    }
  };

  const getStatusText = (status: Status) => {
    switch (status.status) {
      case "PROCESSING":
        return "Processing...";
      case "COMPLETED":
        return "Completed";
      case "FAILED":
        return "Failed";
      default:
        return "Pending";
    }
  };

  const getStatusTitle = (status: Status) => {
    switch (status.type) {
      case "UPLOADED_RESUME":
        return "Uploaded Resume";
      case "GENERATED_RESUME":
        return "Generated Resume";
      case "FREEFORM_ENTRY":
        return "Freeform Entry";
      default:
        return "Resume";
    }
  };

  const getStatusSubtitle = (status: Status) => {
    switch (status.type) {
      case "UPLOADED_RESUME":
        return status.resumeName;
      case "GENERATED_RESUME":
        return status.jobTitle ? 
          status.jobTitle.length > 40 ? 
            `${status.jobTitle.substring(0, 37)}...` : 
            status.jobTitle
          : null;
      default:
        return null;
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric',
      hour12: true
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className="text-xl text-center">Status</DialogTitle>
        </DialogHeader>
        <DialogDescription>
          <div className="flex flex-col gap-4">
            {statuses.length > 0 ? (
              statuses.map((status) => (
                <div
                  key={status.id}
                  className="flex flex-col gap-2 p-4 rounded-lg border bg-card"
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2 max-w-[70%]">
                      {getStatusIcon(status.status)}
                      <div>
                        <h3 className="font-medium text-foreground leading-none">
                          {getStatusTitle(status)}
                        </h3>
                        {getStatusSubtitle(status) && (
                          <p className="text-sm text-muted-foreground mt-1 truncate">
                            {getStatusSubtitle(status)}
                          </p>
                        )}
                        <p className="text-xs text-muted-foreground mt-1">
                          {(status.status == "PROCESSING" || status.status == "PENDING") && (
                            <> Started: {formatDate(status.startedAt)}</>
                          )}
                          {(status.status == "COMPLETED" || status.status == "FAILED") && status.completedAt && (
                            <> Finished: {formatDate(status.completedAt)}</>
                          )}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-sm text-muted-foreground">
                        {status.status != "COMPLETED" && getStatusText(status)}
                      </span>
                      {status.status === "COMPLETED" && (
                        <Button
                          size="sm"
                          variant="outline"
                          className="h-7 px-2"
                          onClick={() => {
                            // TODO: Implement next step action
                            console.log("Next step for", status.id);
                          }}
                        >
                          View
                        </Button>
                      )}
                    </div>
                  </div>
                  {status.error && (
                    <p className="text-sm text-red-500">{status.error}</p>
                  )}
                </div>
              ))
            ) : (
              <div className="text-center py-4 text-muted-foreground">
                No generated resumes found
              </div>
            )}
          </div>
        </DialogDescription>
      </DialogContent>
    </Dialog>
  );
}
