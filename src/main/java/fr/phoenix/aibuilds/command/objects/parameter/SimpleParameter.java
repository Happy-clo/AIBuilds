package fr.phoenix.aibuilds.command.objects.parameter;

public class SimpleParameter extends Parameter {
    public SimpleParameter(String key) {
        super(key, (explorer, list) -> list.add(key));
    }
}
