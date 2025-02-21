package org.thoughtslive.jenkins.plugins.hubot.config;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.CopyOnWriteList;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;

/**
 * Hubot Steps global configuration.
 *
 * @author Naresh Rayapati.
 */
@Extension
public final class GlobalConfig extends GlobalConfiguration {

  private final CopyOnWriteList<HubotSite> sites = new CopyOnWriteList<>();

  public GlobalConfig() {
    load();
  }

  @NonNull
  @Override
  public String getDisplayName() {
    return "Hubot Steps";
  }

  public HubotSite[] getSites() {
    return sites.toArray(new HubotSite[0]);
  }

  public GlobalConfig get() {
    return GlobalConfiguration.all().get(GlobalConfig.class);
  }

  @Override
  public boolean configure(StaplerRequest2 req, JSONObject formData) {
    Stapler.CONVERT_UTILS.deregister(java.net.URL.class);
    Stapler.CONVERT_UTILS.register(new EmptyFriendlyURLConverter(), java.net.URL.class);

    sites.replaceBy(req.bindJSONToList(HubotSite.class, formData.get("sites")));
    save();
    return true;
  }
}
