package com.intershop.cloudops.azure.tasks;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class CreateResourceGroupTask extends AbstractAzureTask
{
    private Property<String> resourceGroupName = getProject().getObjects().property(String.class);
    private Property<Region> resourceGroupRegion = getProject().getObjects().property(Region.class);

    private Property<ResourceGroup> resourceGroup = getProject().getObjects().property(ResourceGroup.class);

    @Input
    public Property<Region> getResourceGroupRegion()
    {
        return this.resourceGroupRegion;
    }

    @Input
    public Property<String> getResourceGroupName()
    {
        return this.resourceGroupName;
    }

    public Property<ResourceGroup> getResourceGroup()
    {
        return (this.resourceGroup);
    }

    @TaskAction
    public void createResourceGroup()
    {
        String rgName = resourceGroupName.get();
        Region rgRegion = resourceGroupRegion.get();

        ResourceGroup rg = getClient().resourceGroups().getByName(rgName);

        int retry = 24;
        while(retry-- > 0 && rg != null && rg.provisioningState().toLowerCase().matches("delet"))
        {
            getProject().getLogger().info("Waiting until the resource group is deleted:  " + rgName);

            try
            {
                Thread.sleep(5000);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        if (rg != null && rg.provisioningState().toLowerCase().matches("delet"))
        {
            throw new GradleException("The test resource group '" + rgName
                            + "' already exist, but is maked as Deleting for a long time.");
        }

        rg = getClient().resourceGroups()
                        .define(rgName)
                        .withRegion(rgRegion)
                        .create();

        this.resourceGroup.set(rg);
    }
}
