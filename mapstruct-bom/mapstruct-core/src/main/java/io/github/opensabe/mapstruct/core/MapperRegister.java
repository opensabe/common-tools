package io.github.opensabe.mapstruct.core;

/**
 * do register with {@link RegisterRepository}
 * @author heng.ma
 */
public abstract class MapperRegister {

    private final MapperRepository repository;

    protected MapperRegister(MapperRepository repository) {
        this.repository = repository;
    }

    protected void register (Class<?> source, Class<?> target, Object mapper) {
        if (mapper instanceof FromMapMapper map) {
            repository.register(target, map);
        }else if (mapper instanceof CommonCopyMapper) {
            if (mapper instanceof SelfCopyMapper self) {
                repository.register(target, self);
            }else {
                repository.register(source, target, (CommonCopyMapper) mapper);
            }
        }
    }

    /**
     * do register by freemarker
     */
    public abstract void register ();

}
