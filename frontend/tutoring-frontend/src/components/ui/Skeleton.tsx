interface SkeletonProps {
  className?: string;
}

export const Skeleton = ({ className = "" }: SkeletonProps) => {
  return <div className={`animate-pulse rounded-lg bg-slate-200/70 ${className}`} />;
};
