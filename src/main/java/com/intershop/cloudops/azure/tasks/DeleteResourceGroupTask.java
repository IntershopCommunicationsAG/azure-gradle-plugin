package com.intershop.cloudops.azure.tasks;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class DeleteResourceGroupTask extends AbstractAzureTask
{
    private Property<String> resourceGroupName = getProject().getObjects().property(String.class);

    @Input
    public Property<String> getResourceGroupName()
    {
        return this.resourceGroupName;
    }

    @TaskAction
    public void deleteResourceGroup()
    {
        String rgName = resourceGroupName.get();

        if (getClient().resourceGroups().contain(rgName))
        {
            getClient().resourceGroups().deleteByName(rgName);

            getLogger().info("ResourceGroup " + rgName + " deleted");
        }
    }
}
