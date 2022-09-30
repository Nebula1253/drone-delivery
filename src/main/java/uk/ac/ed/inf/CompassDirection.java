package uk.ac.ed.inf;

/**
 * Enum values representing the compass directions that the drone is allowed to fly in
 */
//TODO: should this just be a part of LngLat?
public enum CompassDirection {
    EAST,
    EAST_NORTH_EAST,
    NORTH_EAST,
    NORTH_NORTH_EAST,
    NORTH,
    NORTH_NORTH_WEST,
    NORTH_WEST,
    WEST_NORTH_WEST,
    WEST,
    WEST_SOUTH_WEST,
    SOUTH_WEST,
    SOUTH_SOUTH_WEST,
    SOUTH,
    SOUTH_SOUTH_EAST,
    SOUTH_EAST,
    EAST_SOUTH_EAST
}
