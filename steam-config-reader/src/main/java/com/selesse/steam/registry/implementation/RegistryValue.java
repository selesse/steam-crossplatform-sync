package com.selesse.steam.registry.implementation;

/**
 * A value in a parsed VDF file: either a nested block, or a leaf string.
 *
 * <p>Sealed so that a {@code switch} over the two cases is checked for exhaustiveness rather than
 * needing an unreachable {@code else}.
 */
public sealed interface RegistryValue permits RegistryObject, RegistryString {}
