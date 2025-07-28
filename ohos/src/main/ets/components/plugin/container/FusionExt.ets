import FusionConstant from '../constant/FusionConstant'

export default function buildFusionParams(
  routeName: string,
  routeArgs: Map<string, Object> | null = null,
  transparent: boolean = false,
  backgroundColor: number = 0xFFFFFFFF
): Record<string, Object> {
  const record: Record<string, Object> = {}
  record[FusionConstant.ROUTE_NAME] = routeName
  if (routeArgs) {
    record[FusionConstant.ROUTE_ARGS] = mapToRecord(routeArgs)
  }
  record[FusionConstant.EXTRA_BACKGROUND_MODE] = transparent
  record[FusionConstant.EXTRA_BACKGROUND_COLOR] = backgroundColor
  return record
}

export function recordToMap<T>(record: Record<string, T>): Map<string, T> {
  return new Map(Object.entries(record))
}

export function mapToRecord<T>(map: Map<string, T>): Record<string, T> {
  const record: Record<string, T> = {}
  for (let key of map.keys()) {
    record[key] = map.get(key)!
  }
  return record
}