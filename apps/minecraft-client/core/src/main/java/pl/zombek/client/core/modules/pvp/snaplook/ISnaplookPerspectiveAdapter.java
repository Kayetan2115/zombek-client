package pl.zombek.client.core.modules.pvp.snaplook;

/**
 * Adapter interface implemented by platform loaders to query and manipulate game perspective.
 */
public interface ISnaplookPerspectiveAdapter {
    SnaplookPerspective getPerspective();
    void setPerspective(SnaplookPerspective perspective);
}
