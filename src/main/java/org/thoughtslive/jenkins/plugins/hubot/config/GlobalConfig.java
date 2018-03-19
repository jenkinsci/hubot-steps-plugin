package org.thoughtslive.jenkins.plugins.hubot.config;

import hudson.Extension;
import hudson.util.CopyOnWriteList;
import jenkins.model.GlobalConfiguration;
import lombok.Getter;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Hubot Steps global configuration.
 *
 * @author Naresh Rayapati.
 */
@Extension
public final class GlobalConfig extends GlobalConfiguration {

  @Getter
  private final CopyOnWriteList<HubotSite> sites = new CopyOnWriteList<>();

  public GlobalConfig() {
    load();
  }

  @Override
  public String getDisplayName() {
    return "Hubot Steps: Config";
  }

  public HubotSite[] getSites() {
    return sites.toArray(new HubotSite[0]);
  }

  public GlobalConfig get() {
    return GlobalConfiguration.all().get(GlobalConfig.class);
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject formData) {
    Stapler.CONVERT_UTILS.deregister(java.net.URL.class);
    Stapler.CONVERT_UTILS.register(new EmptyFriendlyURLConverter(), java.net.URL.class);

    sites.replaceBy(req.bindJSONToList(HubotSite.class, formData.get("sites")));
    save();
    return true;
  }
}
