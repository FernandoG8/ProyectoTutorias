import type { SVGProps } from "react";

interface IconProps extends SVGProps<SVGSVGElement> {}

const baseProps: Partial<IconProps> = {
  viewBox: "0 0 24 24",
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 1.6,
  strokeLinecap: "round",
  strokeLinejoin: "round",
};

export const HomeIcon = (props: IconProps) => (
  <svg {...baseProps} {...props}>
    <path d="M3 11.5 12 4l9 7.5" />
    <path d="M5.5 10v9h13v-9" />
    <path d="M9.5 21v-6h5v6" />
  </svg>
);

export const UsersIcon = (props: IconProps) => (
  <svg {...baseProps} {...props}>
    <path d="M17 21v-2a4 4 0 0 0-4-4h-2a4 4 0 0 0-4 4v2" />
    <circle cx="12" cy="7" r="3.5" />
    <path d="M21 21v-2a4 4 0 0 0-3-3.87" />
    <path d="M6 15.13A4 4 0 0 0 3 19v2" />
    <path d="M18.5 11a3 3 0 1 0-2-5" />
    <path d="M5.5 11a3 3 0 1 0 2-5" />
  </svg>
);

export const GraduationCapIcon = (props: IconProps) => (
  <svg {...baseProps} {...props}>
    <path d="m3 8 9-5 9 5-9 5-9-5Z" />
    <path d="M12 13v8" />
    <path d="m7 10.5-.5 2.5a5 5 0 0 0 5.5 5" />
    <path d="m17 10.5.5 2.5a5 5 0 0 1-5.5 5" />
  </svg>
);

export const ClipboardListIcon = (props: IconProps) => (
  <svg {...baseProps} {...props}>
    <rect x="5" y="3" width="14" height="18" rx="2.2" />
    <path d="M9 3.5h6" />
    <path d="M9 7h6" />
    <path d="M8 11h1.5" />
    <path d="M8 15h1.5" />
    <path d="M12 11h4" />
    <path d="M12 15h4" />
  </svg>
);

export const FileBarChartIcon = (props: IconProps) => (
  <svg {...baseProps} {...props}>
    <path d="M15.5 3.5H8A2.5 2.5 0 0 0 5.5 6v12A2.5 2.5 0 0 0 8 20.5h8a2.5 2.5 0 0 0 2.5-2.5V8.5Z" />
    <path d="M15.5 3.5V8.5H20" />
    <path d="M10 17v-4" />
    <path d="M12.75 17v-2" />
    <path d="M15.5 17v-6" />
  </svg>
);

export const UserXIcon = (props: IconProps) => (
  <svg {...baseProps} {...props}>
    <path d="M7 21v-1a5 5 0 0 1 5-5" />
    <path d="M12 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0Z" />
    <path d="m17 15 5 5" />
    <path d="m22 15-5 5" />
  </svg>
);

export const SettingsIcon = (props: IconProps) => (
  <svg {...baseProps} {...props}>
    <circle cx="12" cy="12" r="3" />
    <path d="M12 3v2" />
    <path d="M12 19v2" />
    <path d="M3 12h2" />
    <path d="M19 12h2" />
    <path d="m5.6 5.6 1.4 1.4" />
    <path d="m17 17 1.4 1.4" />
    <path d="m18.4 5.6-1.4 1.4" />
    <path d="m7 17-1.4 1.4" />
  </svg>
);

export const UploadCloudIcon = (props: IconProps) => (
  <svg {...baseProps} {...props}>
    <path d="M16 16h1.26A4.74 4.74 0 0 0 22 11.5 4.74 4.74 0 0 0 17.26 7a6 6 0 0 0-11.52 1.5" />
    <path d="M12 16v-7" />
    <path d="m8 12 4-4 4 4" />
    <path d="M6 18h10" />
  </svg>
);

export const MenuIcon = (props: IconProps) => (
  <svg {...baseProps} {...props}>
    <path d="M4 6h16" />
    <path d="M4 12h16" />
    <path d="M4 18h12" />
  </svg>
);
