package org.thoughtslive.jenkins.plugins.hubot.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.Converter;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Hacky converter to handle url while saving configs (Basically copied over from other plugin.)
 */
@Restricted(NoExternalUse.class)
public class EmptyFriendlyURLConverter implements Converter {

  private static final Logger LOGGER = Logger.getLogger(EmptyFriendlyURLConverter.class.getName());

  public Object convert(Class aClass, Object o) {
    if (o == null || "".equals(o) || "null".equals(o)) {
      return null;
    }
    try {
      return new URL(o.toString());
    } catch (MalformedURLException e) {
      LOGGER.log(Level.WARNING, "{0} is not a valid URL", o);
      return null;
    }
  }
}
