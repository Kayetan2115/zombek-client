package pl.zombek.client.core.modules.hud.teamview;

import java.util.List;

/**
 * Interface contract exposing teammate relative bearings and distances for Direction HUD.
 */
public interface ITeamDirectionProvider {
    List<TeammateDirectionInfo> getTeammateDirections();
}
