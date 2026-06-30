export const MAX_FLOOR_PLAN_TABLES = 20

type TableLike = {
  id?: string | number
  name?: string
  number?: string
  posX?: number
  posY?: number
}

export const DEFAULT_TABLE_AREA_ID = 1

export const DEFAULT_TABLE_POSITIONS = [
  { posX: 105, posY: 74 },
  { posX: 275, posY: 74 },
  { posX: 610, posY: 74 },
  { posX: 760, posY: 74 },
  { posX: 1045, posY: 74 },
  { posX: 1215, posY: 74 },
  { posX: 160, posY: 310 },
  { posX: 160, posY: 455 },
  { posX: 600, posY: 315 },
  { posX: 760, posY: 315 },
  { posX: 600, posY: 475 },
  { posX: 760, posY: 475 },
  { posX: 930, posY: 315 },
  { posX: 930, posY: 475 },
  { posX: 1160, posY: 315 },
  { posX: 1160, posY: 475 },
  { posX: 290, posY: 720 },
  { posX: 450, posY: 720 },
  { posX: 850, posY: 720 },
  { posX: 1010, posY: 720 },
]

export const getDefaultTablePosition = (tableNumber: number) =>
  DEFAULT_TABLE_POSITIONS[tableNumber - 1] || { posX: 20, posY: 20 }

const useSavedPosition = (position: number | undefined) =>
  typeof position === 'number' && position > 0

export const getDefaultTableName = (tableNumber: number) => `Bàn ${tableNumber}`

export const STANDARD_TABLE_NUMBERS = Array.from(
  { length: MAX_FLOOR_PLAN_TABLES },
  (_, index) => index + 1,
)

export const getTableLabel = (table: TableLike) =>
  String(table.name || table.number || table.id || '')

export const getStandardTableNumber = (table: TableLike) => {
  const match = getTableLabel(table)
    .trim()
    .match(/^Bàn\s*(\d+)$/i)
  if (!match) return null

  const tableNumber = Number(match[1])
  return tableNumber >= 1 && tableNumber <= MAX_FLOOR_PLAN_TABLES ? tableNumber : null
}

export const naturalTableCompare = (a: TableLike, b: TableLike) =>
  getTableLabel(a).localeCompare(getTableLabel(b), 'vi', {
    numeric: true,
    sensitivity: 'base',
  })

export const normalizeStandardTables = <T extends TableLike>(tables: T[]) => {
  const byNumber = new Map<number, T>()

  ;[...tables].sort(naturalTableCompare).forEach((table) => {
    const tableNumber = getStandardTableNumber(table)
    if (tableNumber && !byNumber.has(tableNumber)) {
      byNumber.set(tableNumber, table)
    }
  })

  return STANDARD_TABLE_NUMBERS.map((tableNumber) => {
    const table = byNumber.get(tableNumber)
    if (!table) return null

    const defaultPosition = getDefaultTablePosition(tableNumber)
    return {
      ...table,
      name: getDefaultTableName(tableNumber),
      number: getDefaultTableName(tableNumber),
      posX: useSavedPosition(table.posX) ? table.posX : defaultPosition.posX,
      posY: useSavedPosition(table.posY) ? table.posY : defaultPosition.posY,
    }
  }).filter(Boolean) as T[]
}

export const limitFloorPlanTables = normalizeStandardTables
