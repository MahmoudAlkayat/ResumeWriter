export function Logo({ className }: { className?: string }) {
  return (
      <svg width="200" height="60" viewBox="0 0 230 60" fill="none" xmlns="http://www.w3.org/2000/svg">
        {/* Document-shaped "E" */}
        <rect x="11" y="11" width="26" height="35" rx="4" fill="white" stroke="black" strokeWidth="1"/>
        <line x1="15" y1="19" x2="34" y2="19" stroke="black" strokeWidth="5"/>
        <line x1="15" y1="29" x2="34" y2="29" stroke="black" strokeWidth="5"/>
        <line x1="15" y1="39" x2="34" y2="39" stroke="black" strokeWidth="5"/>

        {/* "lite" in Elite */}
        <text x="39" y="42" fontFamily="Arial, sans-serif" fontSize="34" fontWeight="bold" className="fill-foreground">lite</text>
        
        {/* "Resume" */}
        <text x="95" y="42" fontFamily="Arial, sans-serif" fontSize="34" fontWeight="bold" fill="#2563EB">Resume</text>
      </svg>
  );
}

