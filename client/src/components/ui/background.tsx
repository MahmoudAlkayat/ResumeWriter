"use client";

import { cn } from "@/lib/utils";

interface BackgroundProps {
  children: React.ReactNode;
  className?: string;
}

export function Background({ children, className }: BackgroundProps) {
  return (
    <div className={cn(
      "bg-gradient-to-br from-blue-200 via-white to-gray-100 dark:from-neutral-800 dark:via-neutral-900 dark:to-neutral-800 overflow-hidden",
      className
    )}>
      {children}
    </div>
  );
}
